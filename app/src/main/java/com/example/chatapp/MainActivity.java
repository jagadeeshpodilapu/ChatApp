package com.example.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.utils.Posts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    Toolbar toolbar;
    DrawerLayout drawerLayout;

    NavigationView navigationView;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserRef, postRef, likeRef, commentRef;
    StorageReference postImageRef;
    String profileImageUrl, currentUsername;

    CircleImageView profileImageView;
    TextView username;

    ImageView addImagePost, sendImagePost;
    EditText inputPostDesc;
    Uri uri;
    ProgressDialog mLoading;
    String postDesc;
    private static final int REQUEST_CODE = 101;

    FirebaseRecyclerAdapter<Posts, MyViewHolder> adapter;
    FirebaseRecyclerOptions<Posts> options;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.appBar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat App");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);

        addImagePost = findViewById(R.id.addImagePost);
        sendImagePost = findViewById(R.id.sendPostImageView);
        inputPostDesc = findViewById(R.id.inputPostDesc);

        mLoading = new ProgressDialog(MainActivity.this);

        View view = navigationView.inflateHeaderView(R.layout.nav_header);

        profileImageView = view.findViewById(R.id.navHeaderProfileImageView);
        username = view.findViewById(R.id.usernameHeader);
        recyclerView = findViewById(R.id.recyclerView);


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        commentRef = FirebaseDatabase.getInstance().getReference().child("Comments");

        postImageRef = FirebaseStorage.getInstance().getReference().child("PostImages");

        navigationView.setNavigationItemSelectedListener(this);

        addImagePost.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            startActivityForResult(i, REQUEST_CODE);
        });

        sendImagePost.setOnClickListener(v -> addPost());

        addImagePost.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE);
        });


        loadPosts();

    }

    private void loadPosts() {

        options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(postRef, Posts.class).build();

        adapter = new FirebaseRecyclerAdapter<Posts, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Posts model) {

                final String postKey = getRef(position).getKey();
                holder.username.setText(model.getUsername());
                String timeAgo = calculateTimeAgo(model.getDatePost());
                holder.timeAgo.setText(timeAgo);
                holder.postDesc.setText(model.getPostDesc());
                Picasso.get().load(model.getPostImageUrl().trim()).into(holder.postImage);
                Picasso.get().load(model.getUserProfileImageUrl().trim()).into(holder.profileImageView);


                holder.countLikes(postKey, mUser.getUid(), likeRef);

                holder.likeImage.setOnClickListener(v -> likeRef.child(postKey).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            likeRef.child(postKey).child(mUser.getUid()).removeValue();
                            holder.likeImage.setColorFilter(Color.BLACK);
                            notifyDataSetChanged();
                        } else {
                            likeRef.child(postKey).child(mUser.getUid()).setValue("like");
                            holder.likeImage.setColorFilter(Color.GREEN);
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));

                holder.sendComment.setOnClickListener(v -> {
                    String comment = holder.inputComments.getText().toString();

                    if (comment.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please enter something", Toast.LENGTH_SHORT).show();
                    } else {
                        AddComment(holder,postKey, commentRef, mUser.getUid(), comment);
                    }
                });


            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_post, parent, false);
                return new MyViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void AddComment(MyViewHolder holder, String postKey, DatabaseReference commentRef, String uid, String comment) {

        HashMap hashMap = new HashMap();

        hashMap.put("username", currentUsername);
        hashMap.put("profileImageUrl", profileImageUrl);
        hashMap.put("comment", comment);

        commentRef.child(postKey).child(uid).updateChildren(hashMap).addOnCompleteListener(task -> {

            if(task.isSuccessful()){
                Toast.makeText(MainActivity.this, "comment added successfully", Toast.LENGTH_SHORT).show();
               adapter.notifyDataSetChanged();
               holder.inputComments.setText("");

            }else{
                Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
            }

        });

    }

    private String calculateTimeAgo(String datePost) {
        /*SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        try {
            long time = sdf.parse(datePost).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago =
                    DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);

            return ago+"";
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                Locale.getDefault());
        Date currentLocalTime = calendar.getTime();

        DateFormat date = new SimpleDateFormat(datePost, Locale.getDefault());
        String localTime = date.format(currentLocalTime);

        return localTime;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            uri = data.getData();
            addImagePost.setImageURI(uri);
        }
    }

    private void addPost() {

        postDesc = inputPostDesc.getText().toString();
        if (postDesc.isEmpty() || inputPostDesc.length() < 3) {
            inputPostDesc.setError("Please write something");
        } else if (uri == null) {
            Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT).show();
        } else {
            mLoading.setTitle("Adding Post");
            mLoading.setMessage("Please Wait...");
            mLoading.setCanceledOnTouchOutside(false);
            mLoading.show();

            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
            final String strDate = formatter.format(date);


            postImageRef.child(mUser.getUid() + strDate).putFile(uri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    postImageRef.child(mUser.getUid() + strDate).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {


                            HashMap hashMap = new HashMap();

                            hashMap.put("datePost", strDate);
                            hashMap.put("postImageUrl", uri.toString());
                            hashMap.put("postDesc", postDesc);
                            hashMap.put("userProfileImageUrl", profileImageUrl);
                            hashMap.put("username", currentUsername);

                            postRef.child(mUser.getUid() + strDate).updateChildren(hashMap).addOnCompleteListener(task1 -> {

                                if (task1.isSuccessful()) {
                                    mLoading.dismiss();
                                    Toast.makeText(MainActivity.this, "Post Added", Toast.LENGTH_SHORT).show();
                                    addImagePost.setImageResource(R.drawable.ic_add_post);
                                    inputPostDesc.setText("");

                                } else {
                                    mLoading.dismiss();
                                    Toast.makeText(MainActivity.this, task1.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } else {
                    mLoading.dismiss();
                    Toast.makeText(MainActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                }

            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mUser == null) {
            sendUserLoginActivity();
        } else {

            mUserRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()) {

                        profileImageUrl = Objects.requireNonNull(snapshot.child("profileImage").getValue()).toString();

                        currentUsername = Objects.requireNonNull(snapshot.child("username").getValue()).toString();

                        Picasso.get().load(profileImageUrl).into(profileImageView);
                        username.setText(currentUsername);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void sendUserLoginActivity() {

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.profile:
                Toast.makeText(this, "profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.friend:
                Toast.makeText(this, "Friend", Toast.LENGTH_SHORT).show();
                break;
            case R.id.findFriend:
                Toast.makeText(this, "Find Friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.chat:
                Toast.makeText(this, "Chat", Toast.LENGTH_SHORT).show();
                break;
            case R.id.logout:
                Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
                break;


        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return true;
    }
}
