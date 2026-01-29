package org.lgp.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import org.lgp.Entity.User;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class UserService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(UserService.class);
    @Inject
    Firestore firestore;

    @Inject
    FirebaseAuth firebaseAuth;

    @Inject
    Logger logger;

    public String registerUser(User.RegisterRequestDTO request) throws FirebaseAuthException {

        logger.infof("Attempting to create Auth user for email: %s", request.email());

        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(request.email())
                .setPassword(request.password())
                .setDisplayName(request.name() + " " + request.lname());

        UserRecord userRecord = firebaseAuth.createUser(createRequest);
        String uid = userRecord.getUid();

        try {
            User user = new User(uid, request.name(), request.lname(), request.email());

            ApiFuture<WriteResult> future = firestore.collection("users")
                    .document(uid)
                    .set(user);

            future.get();

            logger.infof("Successfully registered user with UID: %s", uid);

            return uid;
        } catch (Exception e) {

            logger.errorf("Failed to create Firestore profile for UID %s. Initiating rollback...", uid);

            try {
                firebaseAuth.deleteUser(uid);

                logger.infof("Rollback successful: Deled orphaned Auth user %s", uid);
            } catch (FirebaseAuthException f) {
                logger.fatalf("Rollback failed for UID %s. System is in inconsistent state.", uid);
            }
            throw new RuntimeException("Registration failed due to a system error. Please try again.");
        }
    }

    public User getUser(String uid) {
        try {
            DocumentSnapshot document = firestore.collection("users").document(uid).get().get();

            if (document.exists()) {
                return document.toObject(User.class);
            } else {
                throw new NotFoundException("User profile not found for UID: " + uid);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch user profile", e);
        }
    }
}
