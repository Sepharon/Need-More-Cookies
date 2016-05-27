package sersilinc.needmorecookies;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;


/**
 * This test tests the AddItem activity
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddItemTests {

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
    public ActivityTestRule<AddItem> mActivityRule = new ActivityTestRule<AddItem>(
            AddItem.class) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext();
            Intent result = new Intent(targetContext, AddItem.class);
            result.putExtra("Edit", "False");
            return result;
        }
    };

    //Test the Save button behaviour
    @Test
    public void save_without_product(){
        try{
            onView(withId(R.id.save_item)).perform(click());
        }catch (PerformException e){
            //View is not in hierarchy
        }
        onView(withId(R.id.product))
                .perform(typeText("Cookies"), closeSoftKeyboard());
        try{
            onView(withId(R.id.save_item)).perform(click());
        }catch (PerformException e){
            //View is not in hierarchy
        }
        onView(withId(R.id.quantity))
                .perform(typeText("4"), closeSoftKeyboard());

        // Click on the Spinner
        onView(withId(R.id.type)).perform(click());

        // Click on the second item from the list, which is a marker string: "Vegetables"
        onData(allOf(is(instanceOf(String.class)))).atPosition(4).perform(click());

        onView(withId(R.id.save_item)).perform(click());
    }

    //Enter a product
    @Test
    public void enter_Product() {
        // Type text and then press the button.
        onView(withId(R.id.product))
                .perform(typeText("Carrots"), closeSoftKeyboard());
        onView(withId(R.id.quantity))
                .perform(typeText("2"), closeSoftKeyboard());
        onView(withId(R.id.price))
                .perform(typeText("5.67"), closeSoftKeyboard());

        // Click on the Spinner
        onView(withId(R.id.type)).perform(click());

        // Click on the second item from the list, which is a marker string: "Vegetables"
        onData(allOf(is(instanceOf(String.class)))).atPosition(1).perform(click());

        onView(withId(R.id.save_item)).perform(click());
    }
}