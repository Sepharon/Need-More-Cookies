package sersilinc.needmorecookies;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.widget.Toast;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


/**
 * This test tests the AddList activity
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddListTests {

    /**
     * A JUnit {@link Rule @Rule} to launch your activity under test. This is a replacement
     * for {@link ActivityInstrumentationTestCase2}.
     * <p>
     * Rules are interceptors which are executed for each test method and will run before
     * any of your setup code in the {@link Before @Before} method.
     * <p>
     * {@link ActivityTestRule} will create and launch of the activity for you and also expose
     * the activity under test. To get a reference to the activity you can use
     * the {@link ActivityTestRule#getActivity()} method.
     */
    @Rule
    public ActivityTestRule<AddList> mActivityRule = new ActivityTestRule<>(
            AddList.class);

    //Create private shopping list
    @Test
    public void create_PrivateList() {
        // Type text and then press the button.
        onView(withId(R.id.list_name))
                .perform(typeText("Private List"), closeSoftKeyboard());
        onView(withId(R.id.private_switch)).perform(click());

        // Check that the text was changed.
        onView(withId(R.id.list_name)).check(matches(withText("Private List")));

        onView(withId(R.id.save)).perform(click());
    }

    //Create public shopping list
    @Test
    public void create_PublicList() {
        // Type text and then press the button.
        onView(withId(R.id.list_name))
                .perform(typeText("Public List"), closeSoftKeyboard());
        onView(withId(R.id.public_switch)).perform(click());

        // Check that the text was changed.
        onView(withId(R.id.list_name)).check(matches(withText("Public List")));

        onView(withId(R.id.save)).perform(click());
    }

    //Test the save button behaviour
    @Test
    public void save_without_list() {
        try{
            onView(withId(R.id.save)).perform(click());
        }catch (PerformException e){
            //View is not in hierarchy
        }
        // Type text and then press the button.
        onView(withId(R.id.list_name))
                .perform(typeText("Shopping List"), closeSoftKeyboard());

        try{
            onView(withId(R.id.save)).perform(click());
        }catch (PerformException e){
            //View is not in hierarchy
        }

        onView(withId(R.id.public_switch)).perform(click());

        onView(withId(R.id.save)).perform(click());
    }
}
