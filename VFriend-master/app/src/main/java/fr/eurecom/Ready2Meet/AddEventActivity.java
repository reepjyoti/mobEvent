package fr.eurecom.Ready2Meet;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class AddEventActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth auth;

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add_event);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_add_event);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() != null) {
            View header = navigationView.getHeaderView(0);

            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                // UID specific to the provider
                String uid = user.getUid();

                final TextView textforname = (TextView) header.findViewById(R.id.textView);
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Users/" + uid + "/DisplayName");
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        String value = dataSnapshot.getValue(String.class);

                        if (value == null) {
                            textforname.setText("Not Defined");
                        } else {
                            textforname.setText(value);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value

                    }
                });

                TextView text2 = (TextView) header.findViewById(R.id.textView2);
                text2.setText(user.getEmail());

                final ImageView imgview = (ImageView) header.findViewById(R.id.imageView);
                myRef = database.getReference("Users/" + uid + "/ProfilePictureURL");
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        String value = dataSnapshot.getValue(String.class);
                        Picasso.with(getApplicationContext()).load(value).fit().into(imgview);

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                    }
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        setToolbar();

        Button createEventButton = (Button) findViewById(R.id.createevent);

        createEventButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/title");
                myRef.setValue(((EditText) findViewById(R.id.edittext_title)).getText().toString());

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/description");
                myRef.setValue(((EditText) findViewById(R.id.edittext_description)).getText().toString());

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/time");
                myRef.setValue(((EditText) findViewById(R.id.edittext_time)).getText().toString());

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/place");
                myRef.setValue(((EditText) findViewById(R.id.edittext_place)).getText().toString());

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/capacity");
                myRef.setValue(((EditText) findViewById(R.id.edittext_capacity)).getText().toString());

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/category");
                myRef.setValue(((EditText) findViewById(R.id.edittext_category)).getText().toString());

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/picture");
                myRef.setValue(((EditText) findViewById(R.id.edittext_picture)).getText().toString());

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/current");
                myRef.setValue("1");

                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                final DatabaseReference myRef3 = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/owner");


                DatabaseReference myRef2 = database.getReference("Users/"+user.getUid()+"/DisplayName");
                myRef2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        String value = dataSnapshot.getValue(String.class);
                        myRef3.setValue(value);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value

                    }
                });

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/WhoReported");
                myRef.setValue("");

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/Participants/"+user.getUid());
                myRef.setValue("");

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment;
        if (id == R.id.nav_add_event) {
            // Do nothing
        } else if (id == R.id.nav_allevents) {

            // TODO: Select fragment
            startActivity(new Intent(AddEventActivity.this, Main2Activity.class));

        } else if (id == R.id.nav_messages) {

        } else if (id == R.id.nav_manage) {

            // TODO: Select fragment
            startActivity(new Intent(AddEventActivity.this, Main2Activity.class));

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_add_event);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
