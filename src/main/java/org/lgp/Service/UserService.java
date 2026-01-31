package org.lgp.Service;

import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.lgp.Entity.User;
import org.lgp.Entity.User.UserProfileResponseDTO;
import org.lgp.Entity.User.UserSearchCriteria;
import org.lgp.Entity.User.RegisterRequestDTO;
import org.lgp.Entity.User.UpdateProfileRequestDTO;
import org.lgp.Entity.User.UpdateEmailRequestDTO;
import org.lgp.Entity.User.UpdatePasswordRequestDTO;
import org.lgp.Entity.User.UpdateRolesRequestDTO;
import org.lgp.Exception.ResourceNotFoundException;
import org.lgp.Exception.ServiceException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserService {

    private static final String COLLECTION = "users";

    @Inject
    Firestore firestore;

    @Inject
    FirebaseAuth firebaseAuth;

    @Inject
    Logger logger;

    // =========================================================================
    // PUBLIC METHODS
    // =========================================================================

    public String registerUser(RegisterRequestDTO request) throws FirebaseAuthException {
        logger.infof("Attempting to create Auth user for email: %s", request.email());

        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(request.email())
                .setPassword(request.password())
                .setDisplayName(request.name() + " " + request.lname());

        UserRecord userRecord = firebaseAuth.createUser(createRequest);
        String uid = userRecord.getUid();

        try {
            Map<String, Object> claims = Map.of("roles", List.of(User.Role.USER.getValue()));
            firebaseAuth.setCustomUserClaims(uid, claims);
        } catch (FirebaseAuthException e) {
            logger.error("Failed to set default roles for user " + uid, e);
        }

        try {
            User user = new User(uid, request.name(), request.lname(), request.email());
            firestore.collection(COLLECTION).document(uid).set(user).get();
            logger.infof("Successfully registered user with UID: %s", uid);
            return uid;
        } catch (InterruptedException | ExecutionException e) {
            logger.errorf("Failed to create Firestore profile for UID %s. Initiating rollback...", uid);
            try {
                firebaseAuth.deleteUser(uid);
                logger.infof("Rollback successful: Deleted orphaned Auth user %s", uid);
            } catch (FirebaseAuthException f) {
                logger.fatalf("Rollback failed for UID %s. System is in inconsistent state.", uid);
            }
            throw new ServiceException("Registration failed due to database error", e);
        }
    }

    public List<UserProfileResponseDTO> searchUsers(UserSearchCriteria criteria) {
        try {
            Query query = firestore.collection(COLLECTION);

            if (criteria.id() != null) query = query.whereEqualTo("uid", criteria.id());
            if (criteria.name() != null) query = query.whereEqualTo("name", criteria.name());
            if (criteria.lname() != null) query = query.whereEqualTo("lname", criteria.lname());
            if (criteria.email() != null) query = query.whereEqualTo("email", criteria.email());
            if (criteria.role() != null) query = query.whereArrayContains("roles", criteria.role().getValue());

            return query.get().get().getDocuments().stream()
                    .map(this::mapEntityToResponse).collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("User search failed", e);
        }
    }

    public void updateProfile(String uid, UpdateProfileRequestDTO request) {
        try {
            DocumentSnapshot snapshot = firestore.collection(COLLECTION).document(uid).get().get();
            if (!snapshot.exists()) {
                throw new ResourceNotFoundException("User not found: " + uid);
            }

            User user = snapshot.toObject(User.class);
            boolean changed = false;

            if (request.name() != null && !request.name().isBlank()) {
                user.setName(request.name());
                changed = true;
            }
            if (request.lname() != null && !request.lname().isBlank()) {
                user.setLname(request.lname());
                changed = true;
            }

            if (changed) {
                try {
                    UserRecord.UpdateRequest authUpdate = new UserRecord.UpdateRequest(uid)
                            .setDisplayName(user.getName() + " " + user.getLname());
                    firebaseAuth.updateUser(authUpdate);
                } catch (FirebaseAuthException e) {
                    logger.warn("Failed to sync Display Name to Auth", e);
                }
                firestore.collection(COLLECTION).document(uid).set(user).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to update profile", e);
        }
    }

    public void updateEmail(String uid, UpdateEmailRequestDTO request) {
        String oldEmail = null;
        try {
            UserRecord userRecord = firebaseAuth.getUser(uid);
            oldEmail = userRecord.getEmail();

            if (oldEmail.equalsIgnoreCase(request.email())) {
                return;
            }

            UserRecord.UpdateRequest authUpdate = new UserRecord.UpdateRequest(uid).setEmail(request.email());
            firebaseAuth.updateUser(authUpdate);

            firestore.collection(COLLECTION).document(uid).update("email", request.email()).get();
            logger.infof("Email updated successfully for user %s", uid);

        } catch (FirebaseAuthException e) {
            if (e.getErrorCode().toString().equals("EMAIL_ALREADY_EXISTS")) {
                throw new ServiceException("Email already in use", e);
            }
            throw new ServiceException("Failed to update email in Auth", e);
        } catch (InterruptedException | ExecutionException e) {
            logger.errorf("DB update failed for %s. Rolling back Auth email...", uid);
            try {
                UserRecord.UpdateRequest rollback = new UserRecord.UpdateRequest(uid).setEmail(oldEmail);
                firebaseAuth.updateUser(rollback);
                logger.info("Rollback successful. System state restored.");
            } catch (FirebaseAuthException rollbackEx) {
                logger.fatalf("CRITICAL: DATA INCONSISTENCY! User %s has email %s in Auth but old email in DB.", uid, request.email());
            }
            throw new ServiceException("Failed to sync email to database. Changes reverted.", e);
        }
    }

    public void updatePassword(String uid, UpdatePasswordRequestDTO request) {
        try {
            UserRecord.UpdateRequest authUpdate = new UserRecord.UpdateRequest(uid).setPassword(request.password());
            firebaseAuth.updateUser(authUpdate);
        } catch (FirebaseAuthException e) {
            throw new ServiceException("Failed to update password", e);
        }
    }

    public void updateRoles(String uid, UpdateRolesRequestDTO request) {
        try {
            User updatedUser = firestore.runTransaction(transaction -> {
                DocumentReference docRef = firestore.collection(COLLECTION).document(uid);
                DocumentSnapshot snapshot = transaction.get(docRef).get();
                if (!snapshot.exists()) {
                    throw new ResourceNotFoundException("User not found: " + uid);
                }
                User user = snapshot.toObject(User.class);
                user.setRoles(request.roles());
                transaction.set(docRef, user);
                return user;
            }).get();

            try {
                Map<String, Object> claims = Map.of("roles", updatedUser.getRolesDb());
                firebaseAuth.setCustomUserClaims(uid, claims);
                logger.infof("Roles updated and claims synced for user %s", uid);
            } catch (FirebaseAuthException e) {
                logger.warnf("Roles saved to DB for %s, but Auth Claims sync failed.", uid);
            }
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof ResourceNotFoundException) {
                throw (ResourceNotFoundException) e.getCause();
            }
            throw new ServiceException("Failed to update user roles transactionally", e);
        }
    }

    public void deleteUser(String uid) {
        try {
            // TODO: Check if user has active Loans
            firestore.collection(COLLECTION).document(uid).delete().get();
            try {
                firebaseAuth.deleteUser(uid);
            } catch (FirebaseAuthException e) {
                logger.error("Failed to delete Firebase Auth user after Firestore deletion: " + uid, e);
                throw new ServiceException("Partial deletion failure", e);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to delete user", e);
        }
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private UserProfileResponseDTO mapEntityToResponse(DocumentSnapshot doc) {
        User user = doc.toObject(User.class);
        if (user == null) return null;

        return new UserProfileResponseDTO(
                doc.getId(),
                user.getEmail(),
                user.getName(),
                user.getLname(),
                user.getRoles()
        );
    }
}