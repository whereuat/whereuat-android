package xyz.whereuat.whereuat;

import org.junit.Test;

import xyz.whereuat.whereuat.utils.ContactUtils;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class InitialsUnitTest {
    @Test
    public void two_names() throws Exception {
        String name = "First Last";
        String initials = ContactUtils.getInitials(name);
        assertEquals(initials, "FL");
    }

    @Test
    public void one_name() throws Exception {
        String name = "First";
        String initials = ContactUtils.getInitials(name);
        assertEquals(initials, "F");
    }

    @Test
    public void middle_names() throws Exception {
        String name = "First Middleone Middletwo Middlethree Middlefour Last";
        String initials = ContactUtils.getInitials(name);
        assertEquals(initials, "FL");
    }

    @Test
    public void title() throws Exception {
        String name = "Mr. Last";
        String initials = ContactUtils.getInitials(name);
        assertEquals(initials, "L");
    }

    @Test
    public void title_two_names() throws Exception {
        String name = "Ms. First Last";
        String initials = ContactUtils.getInitials(name);
        assertEquals(initials, "FL");
    }

    @Test
    public void title_no_punctuation() throws Exception {
        String name = "Dr First Last";
        String initials = ContactUtils.getInitials(name);
        assertEquals(initials, "FL");
    }

    @Test
    public void suffix() throws Exception {
        String name = "First Last IV";
        String initials = ContactUtils.getInitials(name);
        assertEquals(initials, "FL");
    }

    @Test
    public void worst_case() throws Exception {
        String name = "Mr. First Middleone Middletwo Last IV PhD";
        String initials = ContactUtils.getInitials(name);
        assertEquals(initials, "FL");
    }
}
