# Firebase Firestore Setup Guide

## Problem
The task creation is working locally but failing to sync to Firebase due to permission errors:
```
Firestore: Write failed at tasks/QuMoC5fhJTH9RdbkhvsM: Status{code=PERMISSION_DENIED, description=Missing or insufficient permissions., cause=null}
```

## Solution

### 1. Deploy Firestore Security Rules

I've created the necessary Firestore security rules in `firestore.rules`. To deploy them:

#### Option A: Using Firebase CLI
```bash
# Install Firebase CLI (if not already installed)
npm install -g firebase-tools

# Login to Firebase
firebase login

# Deploy the rules
firebase deploy --only firestore:rules
```

#### Option B: Using Firebase Console
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Navigate to Firestore Database → Rules
4. Replace the existing rules with the content from `firestore.rules`
5. Click "Publish"

### 2. Current Rules Structure

The rules allow:
- **Users**: Full access to their own user documents
- **Spaces**: Access to spaces where user is owner or member
- **Pages**: Access to pages in spaces where user is owner or member  
- **Tasks**: Access to tasks owned by the user OR in spaces where user is owner or member
- **Space Members**: Owners can manage members, users can manage their own membership

### 3. Key Changes Made

#### Fixed Firebase Repository
- Updated `FirebaseTaskRepository.kt` to use `ownerId` instead of `userId` for filtering
- This matches the `TaskEntity` structure which uses `ownerId` field

#### Updated Security Rules
- Rules now check `resource.data.ownerId == request.auth.uid` for task ownership
- Added fallback access for tasks in spaces where user is a member

#### Sync Service
- `FirebaseSyncService.kt` already sets `ownerId = auth.currentUser?.uid` when creating tasks
- Handles permission failures gracefully by falling back to local-only mode

### 4. Testing

After deploying the rules:

1. Create a new task in the app
2. Check logcat for success messages:
   ```
   TaskViewModel: Inserting task locally: [task title]
   TaskViewModel: Task inserted successfully
   FirebaseSync: Task created in Firebase with ID: [task id]
   ```

3. Verify the task appears in Firestore Console:
   - Go to Firestore Database → Data
   - Check the `tasks` collection
   - Verify the task has the correct `ownerId` field

### 5. Troubleshooting

If still getting permission errors:

1. **Check Firebase Authentication**: Ensure user is logged in
2. **Verify Rules Deployment**: Make sure rules were published successfully
3. **Check Field Names**: Ensure tasks have `ownerId` field set correctly
4. **Test in Console**: Try reading/writing in Firebase Console with the same rules

### 6. Expected Behavior After Fix

- Tasks created locally will sync to Firebase
- Tasks will be associated with the correct user via `ownerId`
- Users can only access their own tasks or tasks in spaces they're members of
- Graceful fallback to local-only mode if Firebase is unavailable
