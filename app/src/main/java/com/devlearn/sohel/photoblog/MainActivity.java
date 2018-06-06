package com.devlearn.sohel.photoblog;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mBlogList;
    private DatabaseReference mDatabase;

    private FirebaseRecyclerAdapter <Blog, BlogViewHolder> firebaseRecyclerAdapter;
    Query databaseReference;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.keepSynced(true);
        mDatabaseUsers.keepSynced(true);

        checkUserExists();
        mBlogList = (RecyclerView) findViewById(R.id.blog_list);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<Blog> options = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(databaseReference,Blog.class)
                .setLifecycleOwner(this)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BlogViewHolder holder, int position, @NonNull Blog model) {
                holder.setDesc(model.getDesc());
                holder.setTitle(model.getTitle());
                holder.setImage(model.getImage());
                holder.setUsername(model.getUsername());

                //applying onclick on whole element
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "You clicked a View", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_row,parent,false);
                return new BlogViewHolder(view);
               // return new BlogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_row,parent,false));
            }
        };

        mBlogList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mAuth.addAuthStateListener(mAuthListener); //add this for logout
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            //checkUserExists();
        } else {
            Intent loginIntent = new Intent(MainActivity.this,RegisterActivity.class);
            //user cant go back
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
        }
//        checkUserExists();

        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public class BlogViewHolder extends RecyclerView.ViewHolder{

        TextView _title;
        TextView _desc;
        ImageView _image;
        TextView postUsername;
        View mView;

        BlogViewHolder(View itemView){
            super(itemView);
            mView = itemView;
            _title = mView.findViewById(R.id.post_title);
            //for applying onclick on sigle element
            _title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v("Mainactivity","some text");
                }
            });

        }

        public void setTitle(String title)
        {
            _title.setText(title);
        }

        public void setDesc(String desc)
        {
            _desc = mView.findViewById(R.id.post_text);

            _desc.setText(desc);
        }

        public void setImage(final String image)
        {

            _image = mView.findViewById(R.id.post_image);
            Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).into(_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(image).into(_image);
                }
            });
        }

        public void setUsername(String username)
        {
            postUsername = mView.findViewById(R.id.postUsername);
            postUsername.setText(username);
        }

    }
    private void checkUserExists() {

        if(mAuth.getCurrentUser() !=null) {
            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild(user_id))
                    {
                        Intent intent = new Intent(MainActivity.this,SetupActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_add){
            startActivity(new Intent(MainActivity.this,PostActivity.class));
        }
        else if(item.getItemId() == R.id.action_settings){
            startActivity(new Intent(MainActivity.this,CommentActivity.class));
        }
        else if(item.getItemId() == R.id.action_logout)
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this,RegisterActivity.class));
        }
        else if(item.getItemId() == R.id.action_setup){
            startActivity(new Intent(MainActivity.this,SetupActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
