package main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.util.concurrent.TimeUnit;

import java.awt.*;

/**
 * Created by jordygroote on 18-12-16.
 */

@ScriptManifest(category = Category.CRAFTING, name = "MoltenGlassSmelter", author = "Jordyownn", version = 1.1)
public class main extends AbstractScript {
//    AREA
    Area BankArea = new Area(3098, 3494,3095,3497,0);
    Area FurnaceArea = new Area(3109,3497,3107,3501,0);

    private final int BucketOfSand = 1783;
    private final int SodaAsh = 1781;

    //GUI
    private long timeBegan;
    private long timeRan;
    private int glassMade;


//    STATE
    private int state = -1;

    @Override
    public void onStart() {
        timeBegan = System.currentTimeMillis();
        glassMade = 0;
        log("Welcome to MoltenGlassSmelter 1.0");
        state = 0;
    }

    @Override
    public int onLoop() {
        if (getDialogues().canContinue()) {
            getDialogues().continueDialogue();
        } else {
            if (state == 0) {
                withdraw();
            } else if (state == 1) {
                smelt();
            } else if (state == 2) {
                bank();
            }
        }
        return Calculations.random(500,1000);
    }

    private void withdraw(){
        if(getInventory().isEmpty()) {
            if (BankArea.contains(getLocalPlayer())) {
                NPC banker = getNpcs().closest(npc -> npc != null && npc.hasAction("Bank"));

                if (!getBank().isOpen()){
                    if (banker != null && banker.interact("Bank")){

                    }
                } else {
                    if(sleepUntil(()-> getBank().open(),9000)) {
                        if(getBank().withdraw(BucketOfSand, 14)){
                            if(sleepUntil(() -> getInventory().contains("Bucket of sand"), 2500)){
                                if(getBank().withdraw(SodaAsh, 14)){
                                    if(sleepUntil(() -> getInventory().contains("Soda ash"), 2500)){
                                        getBank().close();
                                        state = 1;
                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                if (getWalking().walk(BankArea.getCenter())) {
                    sleepUntil(() -> getLocalPlayer().distance(BankArea.getCenter()) < Calculations.random(2, 4), 7000);
                }
            }
        } else {
            state = 1;
        }
    }

    private void smelt(){
        if(getInventory().contains(BucketOfSand)){
            if(FurnaceArea.contains(getLocalPlayer())){
                GameObject Furnace = getGameObjects().closest("Furnace");
                if (Furnace != null && getInventory().count("Molten glass") < 14) {
                    getInventory().get(BucketOfSand).useOn(Furnace);
                    sleep(3000);
                    if (!getWidgets().getWidgetChildrenContainingText("Molten glass").isEmpty()) {
                        WidgetChild child = getWidgets().getWidgetChildrenContainingText("Molten glass").get(0);
                        if (child != null) {
                            if(child.interact("Make All")){
                                sleepWhile( () -> getInventory().contains(BucketOfSand) && !getDialogues().canContinue(), Calculations.random(35000, 40000));
                            }
                        }
                    }
                }
                if(!getInventory().contains(BucketOfSand)){
                    state = 2;
                }


            } else {
                if (getWalking().walk(FurnaceArea.getCenter())) {
                    sleep(Calculations.random(5000,7000));
                }
            }
        } else {
            state = 2;
        }
    }

    private void bank(){
        if(BankArea.contains(getLocalPlayer())) {
            NPC banker = getNpcs().closest(npc -> npc != null && npc.hasAction("Bank"));
            if (banker != null){
                banker.interact("Bank");
                sleep(Calculations.random(2000, 3000));
                if(sleepUntil(()-> getBank().open(),9000)) {
                    if(getBank().depositAllItems()){
                        glassMade += 14;
                        if (sleepUntil(()-> !getInventory().isFull(),4000)){
                            state = 0;
                        }
                    }
                }
            }
        } else {
            if(getWalking().walk(BankArea.getRandomTile())){
                sleep(Calculations.random(5000, 7000));
            }
        }
    }

    @Override
    public void onPaint(Graphics graphics) {
        timeRan = System.currentTimeMillis() - this.timeBegan;
        graphics.drawString(ft(timeRan), 380, 300);
        graphics.drawString("" + glassMade + " moltenglass made", 380, 315);
        super.onPaint(graphics);
    }

    private String ft(long duration)
    {
        String res = "";
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration)
                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                .toHours(duration));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                .toMinutes(duration));
        if (days == 0) {
            res = ("Time Ran:" + hours + ":" + minutes + ":" + seconds);
        } else {
            res = (days + ":" + hours + ":" + minutes + ":" + seconds);
        }
        return res;
    }

    @Override
    public void onExit() {

    }


}
