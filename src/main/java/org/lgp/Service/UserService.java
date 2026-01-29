package org.lgp.Service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import org.lgp.Entity.User;
import org.lgp.Entity.User.UserProfileResponseDTO;
import org.lgp.Entity.User.RegisterRequestDTO;
import org.lgp.Entity.User.UpdateProfileRequestDTO;
import org.lgp.Entity.User.UpdateEmailRequestDTO;
import org.lgp.Entity.User.UpdatePasswordRequestDTO;
import org.lgp.Exception.ResourceNotFoundException;
import org.lgp.Exception.ServiceException;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserService {

    @Inject
    Firestore firestore;

    @Inject
    FirebaseAuth firebaseAuth;

    @Inject
    Logger logger;

    private static final String COLLECTION = "users";

    public String registerUser(RegisterRequestDTO request) throws FirebaseAuthException {

        logger.infof("Attempting to create Auth user for email: %s", request.email());

        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(request.email())
                .setPassword(request.password())
                .setDisplayName(request.name() + " " + request.lname());

        UserRecord userRecord = firebaseAuth.createUser(createRequest);
        String uid = userRecord.getUid();

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

    public UserProfileResponseDTO getUser(String uid) {
        try {
            DocumentSnapshot document = firestore.collection(COLLECTION).document(uid).get().get();

            if (!document.exists()) {
                throw new ResourceNotFoundException("User profile not found for UID: " + uid);
            }

            return mapEntityToResponse(document);

        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to fetch user profile", e);
        }
    }

    public List<UserProfileResponseDTO> getAllUsers() {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION).get().get().getDocuments();

            return documents.stream()
                    .map(this::mapEntityToResponse)
                    .collect(Collectors.toList());

        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to fetch user list", e);
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
        try {
            UserRecord.UpdateRequest authUpdate = new UserRecord.UpdateRequest(uid)
                    .setEmail(request.email());
            firebaseAuth.updateUser(authUpdate);

            firestore.collection(COLLECTION).document(uid).update("email", request.email()).get();

        } catch (FirebaseAuthException e) {
            if (e.getErrorCode().toString().equals("EMAIL_ALREADY_EXISTS")) {
                throw new ServiceException("Email already in use", e);
            }
            throw new ServiceException("Failed to update email in Auth", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Email updated in Auth but failed to sync to Database", e);
        }
    }

    public void updatePassword(String uid, UpdatePasswordRequestDTO request) {
        try {
            UserRecord.UpdateRequest authUpdate = new UserRecord.UpdateRequest(uid)
                    .setPassword(request.password());
            firebaseAuth.updateUser(authUpdate);
        } catch (FirebaseAuthException e) {
            throw new ServiceException("Failed to update password", e);
        }
    }

    public void deleteUser(String uid) {
        try {
            // TODO: Check if user has active Loans (InventoryService check)

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