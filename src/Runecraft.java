import display.ProgressPaint;
import display.RunecraftGUI;
import methods.CraftMethod;
import methods.RunecraftTask;
import org.rspeer.runetek.event.listeners.ItemTableListener;
import org.rspeer.runetek.event.listeners.MouseInputListener;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.ItemTableEvent;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.runetek.providers.RSItemDefinition;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;
import task_structure.TreeScript;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Queue;

@ScriptMeta(name = "DRunecraft", desc = "Crafts runes", developer = "Dungeonqueer", category = ScriptCategory.RUNECRAFTING, version = 1.6)
public class Runecraft extends TreeScript implements RenderListener, ItemTableListener, MouseInputListener {
    private RunecraftGUI runecraftGUI;
    private ProgressPaint progressPaint;
    private boolean willStart;
    private Queue<RunecraftTask> taskQueue;
    private RunecraftTask currentTask;

    @Override
    public void onStart() {
        willStart = getRSPeerUser().getUsername().equals("Dungeonqueer");
        if (willStart) runecraftGUI = new RunecraftGUI();
        super.onStart();
    }

    @Override
    public int loop() {
        if (!willStart) {
            Log.info("I know where you live");
            return -1;
        }
        if (!runecraftGUI.hasBeenSet()) return runecraftGUI.isHidden() ? -1 : 2000;
        if (taskQueue == null) taskQueue = runecraftGUI.getRunecraftTasks();
        if (currentTask == null || currentTask.hasReachedGoal()) {
            if (taskQueue.isEmpty()) return -1;
            else {
                if (currentTask != null) Log.info(progressPaint);
                currentTask = taskQueue.remove();
                Log.info("Current task set to " + currentTask.toString());
                final CraftMethod craftMethod = currentTask.getMethod(this);
                setHead(craftMethod.getHead());
                progressPaint = new ProgressPaint(craftMethod);
            }
            return super.loop();
        }
        final int traversed = traverseTree();
        final boolean badTraverse = traversed == -1;
        if (badTraverse) currentTask = null;
        return badTraverse ? super.loop() : traversed;
    }

    @Override
    public void onStop() {
        final String lastTask = getTaskDescription();
//        if (lastTask != null && !lastTask.isEmpty()) Log.info("Stopped on task: " + lastTask);
        if (progressPaint != null) Log.info(progressPaint);
        if (runecraftGUI != null) runecraftGUI.exit();
        super.onStop();
    }

    @Override
    public void notify(final RenderEvent renderEvent) {
        final Graphics source = renderEvent.getSource();
        final String taskDescription = getTaskDescription();
        if (source == null || progressPaint == null || taskDescription == null) return;
        progressPaint.displayPaint(source, taskDescription);
    }

    @Override
    public void notify(final ItemTableEvent event) {
        final String taskDescription = getTaskDescription();
        if (taskDescription == null || !taskDescription.contains("Crafting")) return;
        final RSItemDefinition objectDefinition = event.getDefinition();
        if (objectDefinition == null) return;
        if (objectDefinition.getName().matches(".+\\srune") && event.getChangeType() == ItemTableEvent.ChangeType.ITEM_ADDED)
            progressPaint.updateRunes(event.getStackSize() - event.getOldStackSize());
        else if (objectDefinition.getName().contains("essence") && event.getChangeType() == ItemTableEvent.ChangeType.ITEM_REMOVED)
            progressPaint.updateEssence();
    }

    @Override
    public void notify(final MouseEvent mouseEvent) {
        if (progressPaint == null || mouseEvent == null) return;
        final Object eventSource = mouseEvent.getSource();
        if (eventSource == null || eventSource.equals("bot") || !mouseEvent.paramString().split(",")[0].equals("MOUSE_RELEASED"))
            return;
        progressPaint.toggleIfMet(mouseEvent.getPoint());
    }
}
