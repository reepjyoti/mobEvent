package fr.eurecom.Ready2Meet;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import fr.eurecom.Ready2Meet.database.Event;
import fr.eurecom.Ready2Meet.database.User;

public class AddEventActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SimpleDateFormat format = new SimpleDateFormat("EE, MMM dd, yyyy 'at' hh:mm a");
    private Calendar startDate = null;
    private int PLACE_PICKER_REQUEST = 1;

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
                String uid = user.getUid();

                final TextView textforname = (TextView) header.findViewById(R.id.textView);
                final ImageView imgview = (ImageView) header.findViewById(R.id.imageView);

                TextView text2 = (TextView) header.findViewById(R.id.textView2);
                text2.setText(user.getEmail());

                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Users/" + uid);
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        textforname.setText(user.DisplayName);
                        Picasso.with(getApplicationContext()).load(user.ProfilePictureURL).fit().into(imgview);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // TODO: Error handling
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
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                String eventId = ((EditText) findViewById(R.id.txt_eventid)).getText().toString(); // TODO: Change this to owner + incrementing count

                String eventTitle = ((EditText) findViewById(R.id.edittext_title)).getText().toString();
                String eventDescription = ((EditText) findViewById(R.id.edittext_description)).getText().toString();
                String place = ((Button) findViewById(R.id.find_location_button)).getText().toString();
                Long capacity = Long.parseLong(((EditText) findViewById(R.id.edittext_capacity)).getText().toString());
                String startTime = ((Button) findViewById(R.id.show_starttime_button)).getText().toString();
                String endTime = ((Button) findViewById(R.id.show_endtime_button)).getText().toString();
                Map<String, Boolean> categories = new HashMap<>();
                categories.put(((EditText) findViewById(R.id.edittext_category)).getText().toString(), true);
                String picture = ((EditText) findViewById(R.id.edittext_picture)).getText().toString();
                Long current = Long.valueOf(1);
                Map<String, Boolean> whoReported = new HashMap<>();
                Map<String, Boolean> participants = new HashMap<>();
                participants.put(user.getUid(), true);
                String owner = user.getUid();

                Event newEvent = new Event(eventTitle, eventDescription, owner, current, categories,
                        capacity, picture, place, startTime, endTime,
                        participants, whoReported);

                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("Events").child(eventId).setValue(newEvent);
            }
        });

        final Button showStartTimeButton = (Button) findViewById(R.id.show_starttime_button);
        showStartTimeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final DateTimePickerDialog dialog = new DateTimePickerDialog(AddEventActivity.this);
                dialog.show();

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogI) {
                        Calendar result = dialog.getResult();
                        if(result != null) {
                            showStartTimeButton.setText(format.format(result.getTime()));
                            startDate = result;
                        }
                    }
                });
            }
        });

        final Button showEndTimeButton = (Button) findViewById(R.id.show_endtime_button);
        showEndTimeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final DateTimePickerDialog dialog = new DateTimePickerDialog(AddEventActivity.this);
                dialog.setMinDate(startDate);
                dialog.show();

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogI) {
                        Calendar result = dialog.getResult();
                        if(result != null) {
                            showEndTimeButton.setText(format.format(result.getTime()));
                        }
                    }
                });
            }
        });

        Button getLocationButton = (Button) findViewById(R.id.find_location_button);
        getLocationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(AddEventActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                Button getLocationButton = (Button) findViewById(R.id.find_location_button);
                getLocationButton.setText(place.getAddress());
            }
        }
    }
}
