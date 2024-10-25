package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;

/**
 * A listener that responds to renaming events of Jenkins items (jobs).
 *
 * <p>
 * This class extends {@link ItemListener} and overrides the {@link #onRenamed(Item, String, String)}
 * method to perform actions when a Jenkins job is renamed. The renaming process triggers the
 * {@link hudson.model.AbstractItem#renameTo(String)} method, which subsequently calls
 * {@link ItemListener#onRenamed(Item, String, String)}.
 * </p>
 *
 * @see ItemListener
 * @see hudson.model.AbstractItem
 */
@Extension
public class JobNameRenameListener extends ItemListener {

    /**
     * Called when an item (job) has been renamed.
     *
     * @param item The item that has been renamed.
     * @param oldName The previous name of the item.
     * @param newName The new name of the item.
     */
    @Override
    public void onRenamed(Item item, String oldName, String newName) {
        OnboardingTaskBuilder.renameJobName(oldName, newName);
    }
}
