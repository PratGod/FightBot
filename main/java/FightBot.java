import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.extensions.parsers.HEntity;
import gearth.extensions.parsers.HEntityType;
import gearth.extensions.parsers.HEntityUpdate;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogManager;

@ExtensionInfo(Title = "FightBot", Description = " Magic", Version = "1.14", Author = "PGod")
public class FightBot extends ExtensionForm implements NativeKeyListener {
    @FXML
    Label Label;
    @FXML
    Button Switch;
    @FXML
    Button Switch2;
    @FXML
    CheckBox CheckBox;
    @FXML
    CheckBox Opt;

    private String YourId;
    private String Target;
    private int yourId = -1;
    private int TargetId = -1;
    private int TargetX;
    private int TargetY;
    private boolean isUsernameInitialized;
    private HashMap<Integer, Integer> UserIdAndIndex = new HashMap<>();
    private HashMap<Integer, String> UserIdAndName = new HashMap<>();

    protected void onShow() {
        LogManager.getLogManager().reset();
        try {
            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook();
                System.out.println("Hook enabledd");
            }
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(this);
        sendToServer(new HPacket("InfoRetrieve", HMessage.Direction.TOSERVER));


    }

    protected void onHide() {
        UserIdAndIndex.clear();
        UserIdAndName.clear();
        yourId = -1;
        TargetId = -1;
        TargetX = 0;
        TargetY = 0;
        Platform.runLater(() -> CheckBox.setText("Click the user to follow"));
    }

    protected void initExtension() {


        intercept(HMessage.Direction.TOCLIENT, "UserObject", hMessage -> {
            yourId = hMessage.getPacket().readInteger();
            YourId = hMessage.getPacket().readString().toLowerCase();
            if (this.YourId.equals("usyk")) {
                sendToClient(new HPacket("{in:Chat}{i:999}{s:\"User Whiplash Logged on(FB connected)\"}{i:0}{i:19}{i:0}{i:-1}"));
                this.isUsernameInitialized = true;
            } else {
                sendToClient(new HPacket("{in:Chat}{i:999}{s:\"Who the fuck are you, this wont work for u faggot\"}{i:0}{i:19}{i:0}{i:-1}"));
                this.isUsernameInitialized = false;
                System.out.println("Kys faggot");
            }
            System.out.println("Extracted Username: " + this.YourId + " UserID : " + this.yourId);
        });

        intercept(HMessage.Direction.TOSERVER, "GetSelectedBadges", hMessage -> {
            int selectedId = hMessage.getPacket().readInteger();

            try {
                // Check if the selected ID is valid (not your ID)
                if (selectedId != yourId && !UserIdAndName.get(selectedId).equals(YourId) && CheckBox.isSelected()) {
                    TargetId = selectedId; // Update TargetId to the new valid target
                    Target = UserIdAndName.get(TargetId); // Update Target name
                    Platform.runLater(() -> Label.setText(Target)); // Update UI label
                    System.out.println("New Target ID: " + TargetId + " Name: " + Target);
                } else {
                    System.out.println("Invalid selection: Your own ID or unchecked target.");
                }
            } catch (NullPointerException ignored) {
                System.out.println("Error: Target does not exist.");
            }
        });


        intercept(HMessage.Direction.TOCLIENT, "Users", hMessage -> {
            try {
                HPacket hPacket = hMessage.getPacket();
                HEntity[] roomUsersList = HEntity.parse(hPacket);
                for (HEntity hEntity : roomUsersList) {
                    if (hEntity.getEntityType().equals(HEntityType.HABBO)) {
                        UserIdAndIndex.put(hEntity.getId(), hEntity.getIndex());
                        UserIdAndName.put(hEntity.getId(), hEntity.getName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        intercept(HMessage.Direction.TOSERVER, "GetSelectedBadges", hMessage -> {
            int selectedId = hMessage.getPacket().readInteger();

            try {
                // Checks if the selected id is valid and does not match with MY ID - very annoying but finally updated
                if (selectedId != yourId && !UserIdAndName.get(selectedId).equals(YourId) && CheckBox.isSelected()) {
                    TargetId = selectedId; // Update TargetId to the new valid target
                    Target = UserIdAndName.get(TargetId); // Update Target name
                    Platform.runLater(() -> Label.setText(Target)); // Update UI label
                    System.out.println("New Target ID: " + TargetId + " Name: " + Target);
                } else {
                    System.out.println("Invalid selection: Your own ID or unchecked target.");
                }
            } catch (NullPointerException ignored) {
                System.out.println("Error: Target does not exist.");
            }
        });

        intercept(HMessage.Direction.TOCLIENT, "UserUpdate", hMessage -> {
            try {
                for (HEntityUpdate hEntityUpdate : HEntityUpdate.parse(hMessage.getPacket())) {
                    if (Switch.getText().equals("ON")) {
                        try {
                            // Ensure the target update is valid
                            if (isUsernameInitialized) {
                                if (hEntityUpdate.getIndex() == UserIdAndIndex.get(TargetId) && TargetId != yourId) {
                                    // Update target coordinates (moveavatar again hashed , im done updating this so probably this is the last update EVER RIP COON
                                    TargetX = hEntityUpdate.getMovingTo().getX();
                                    TargetY = hEntityUpdate.getMovingTo().getY();
                                    System.out.println("Original X: " + TargetX + " Y: " + TargetY + " Username: " + Target);

                                    // Convert X and Y coordinates to hexadecimal
                                    String hexX = Integer.toHexString(TargetX).toUpperCase();
                                    String hexY = Integer.toHexString(TargetY).toUpperCase();

                                    // Scrambling for X
                                    String scrambledX = scrambleX(hexX);

                                    // Scrambling for Y
                                    String scrambledY = scrambleY(hexY);

                                    // debuggg output
                                    System.out.println("Scrambled X: " + scrambledX + " Scrambled Y: " + scrambledY);

                                    // Schedule a packet to move avatar to scrambled coordinates
                                    Timer timer = new Timer();
                                    TimerTask task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            sendToServer(new HPacket(String.format("{out:MoveAvatar}{s:\"%s\"}{s:\"%s\"}", scrambledX, scrambledY)));
                                        }
                                    };

                                    timer.schedule(task, 25); // i think 25 is best
                                }
                            }else{
                                sendToClient(new HPacket("{in:Chat}{i:999}{s:\"Connection Closed\"}{i:0}{i:19}{i:0}{i:-1}"));
                            }
                        } catch (NullPointerException ignored) {


                            Random random = new Random();

                            switch (random.nextInt(3))
                            {
                                case 1:
                                    String hexX = Integer.toHexString(TargetX-1).toUpperCase();
                                    String hexY = Integer.toHexString(TargetY).toUpperCase();

                                    // Scrambling for X
                                    String scrambledX = scrambleX(hexX);

                                    // Scrambling for Y
                                    String scrambledY = scrambleY(hexY);

                                    // dbug output
                                    System.out.println("Scrambled X: " + scrambledX + " Scrambled Y: " + scrambledY);

                                    // Schedule a packet to move avatar to scrambled coordinates
                                    Timer timer = new Timer();
                                    TimerTask task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            sendToServer(new HPacket(String.format("{out:MoveAvatar}{s:\"%s\"}{s:\"%s\"}", scrambledX, scrambledY)));

                                        }

                                    };

                                    timer.schedule(task, 25);
                                    break;

                                case 2:
                                     hexX = Integer.toHexString(TargetX+1).toUpperCase();
                                     hexY = Integer.toHexString(TargetY).toUpperCase();

                                    // Scrambling for X
                                     scrambledX = scrambleX(hexX);

                                    // Scrambling for Y
                                     scrambledY = scrambleY(hexY);

                                    // dbug output
                                    System.out.println("Scrambled X: " + scrambledX + " Scrambled Y: " + scrambledY);

                                    // Schedule a packet to move avatar to scrambled coordinates
                                     timer = new Timer();
                                     task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            sendToServer(new HPacket(String.format("{out:MoveAvatar}{s:\"%s\"}{s:\"%s\"}", scrambledX, scrambledY)));
                                        }
                                    };

                                    timer.schedule(task, 25);
                                    break;

                                case 3:
                                    hexX = Integer.toHexString(TargetX).toUpperCase();
                                    hexY = Integer.toHexString(TargetY-1).toUpperCase();

                                    // Scrambling for X
                                    scrambledX = scrambleX(hexX);

                                    // Scrambling for Y
                                    scrambledY = scrambleY(hexY);

                                    // dbug output
                                    System.out.println("Scrambled X: " + scrambledX + " Scrambled Y: " + scrambledY);

                                    // Schedule a packet to move avatar to scrambled coordinates
                                    timer = new Timer();
                                    task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            sendToServer(new HPacket(String.format("{out:MoveAvatar}{s:\"%s\"}{s:\"%s\"}", scrambledX, scrambledY)));
                                        }
                                    };

                                    timer.schedule(task, 25);
                                    break;

                                case 4:
                                    hexX = Integer.toHexString(TargetX).toUpperCase();
                                    hexY = Integer.toHexString(TargetY+1).toUpperCase();

                                    // Scrambling for X
                                    scrambledX = scrambleX(hexX);

                                    // Scrambling for Y
                                    scrambledY = scrambleY(hexY);

                                    // dbug output HOW IS SCRAMBLEX A FUCKING DECIMAL - ok that was a debug mistake LOL
                                    System.out.println("Scrambled X: " + scrambledX + " Scrambled Y: " + scrambledY);

                                    // Schedule a packet to move avatar to scrambled coordinates
                                    timer = new Timer();
                                    task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            sendToServer(new HPacket(String.format("{out:MoveAvatar}{s:\"%s\"}{s:\"%s\"}", scrambledX, scrambledY)));
                                        }
                                    };

                                    timer.schedule(task, 25);
                                    break;
                            }

                            System.out.println("Error: Invalid target update.");
                        }
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("ArrayIndexOutOfBoundsException ignored: " + e.getMessage());
            }
        });


    }

    // Scrambling X coordinates, UPDATED AGAIN AGAIN AGAIN AGAIN AGAIN
    private String scrambleX(String hexX) {
        int xValue = Integer.parseInt(hexX, 16);
        String scrambledX;

        switch (xValue) {
            case 1:
                scrambledX = "198 + 1D9";
                break;
            case 2:
                scrambledX = "1D9 + 1D9";
                break;
            case 3:
                scrambledX = "1D9 + 11E";
                break;
            case 4:
                scrambledX = "11E + 11E";
                break;
            case 5:
                scrambledX = "11E + 14F";
                break;
            case 6:
                scrambledX = "14F + 14F";
                break;
            case 7:
                scrambledX = "14F + 294";
                break;
            case 8:
                scrambledX = "294 + 294";
                break;
            case 9:
                scrambledX = "294 + 2D5";
                break;
            case 10:
                scrambledX = "2D5 + 2D5";
                break;
            case 11:
                scrambledX = "2D5 + 22A";
                break;
            case 12:
                scrambledX = "22A + 22A";
                break;
            case 13:
                scrambledX = "22A + 26B";
                break;
            case 14:
                scrambledX = "26B + 26B";
                break;
            case 15:
                scrambledX = "26B + 3A0";
                break;
            case 16:
                scrambledX = "3A0 + 3A0";
                break;
            case 17:
                scrambledX = "3A0 + 3E1";
                break;
            case 18:
                scrambledX = "3E1 + 3E1";
                break;
            case 19:
                scrambledX = "3E1 + 326";
                break;
            case 20:
                scrambledX = "326 + 326";
                break;
            case 21:
                scrambledX = "326 + 377";
                break;
            case 22:
                scrambledX = "377 + 377";
                break;
            case 23:
                scrambledX = "377 + 4BC";
                break;
            case 24:
                scrambledX = "4BC + 4BC";
                break;
            default:
                scrambledX = String.format("%s + %X", hexX, xValue);
                break;
        }

        return scrambledX;
    }

    // Scrambling Y coordinates
    private String scrambleY(String hexY) {
        int yValue = Integer.parseInt(hexY, 16);
        String scrambledY;

        switch (yValue) {
            case 1:
                scrambledY = "11E - 1D9";
                break;
            case 2:
                scrambledY = "3A0 - 22A";
                break;
            case 3:
                scrambledY = "2D5 - 11E";
                break;
            case 4:
                scrambledY = "294 - 198";
                break;
            case 5:
                scrambledY = "2D5 - 198";
                break;
            case 6:
                scrambledY = "432 - 3A0";
                break;
            case 7:
                scrambledY = "3A0 - 1D9";
                break;
            case 8:
                scrambledY = "326 - 11E";
                break;
            case 9:
                scrambledY = "5C8 - 26B";
                break;
            case 10:
                scrambledY = "57F - 3E1";
                break;
            case 11:
                scrambledY = "54E - 26B";
                break;
            case 12:
                scrambledY = "6C4 - 3A0";
                break;
            case 13:
                scrambledY = "54E - 2D5";
                break;
            case 14:
                scrambledY = "705 - 26B";
                break;
            case 15:
                scrambledY = "473 - 198";
                break;
            case 16:
                scrambledY = "54E - 11E";
                break;
            case 17:
                scrambledY = "609 - 198";
                break;
            case 18:
                scrambledY = "54E - 198";
                break;
            case 19:
                scrambledY = "65A - 14F";
                break;
            case 20:
                scrambledY = "82D - 3E1";
                break;
            case 21:
                scrambledY = "8A7 - 22A";
                break;
            case 22:
                scrambledY = "862 - 3A0";
                break;
            case 23:
                scrambledY = "756 - 14F";
                break;
            case 24:
                scrambledY = "8EC - 294";
                break;
            default:
                scrambledY = String.format("%s + %X", hexY, yValue);
                break;
        }

        return scrambledY;
    }







    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {}

    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {

        if (nativeKeyEvent.getKeyCode() == 56) {
            Simple();
        }
    }

    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {}

    public void Simple() {
        Platform.runLater(() -> {
            if (Switch.getText().equals("ON")) {
                Switch.setText("OFF");
                Switch.setStyle("-fx-background-color: #FF5733");
            } else {
                Switch.setText("ON");
                Switch.setStyle("-fx-background-color: #00A86B");
                Switch2.setText("OFF");
                Switch2.setStyle("-fx-background-color: #FF5733");
            }
        });
    }
    public void handleClose() {
        System.out.println("Close button clicked");

        System.exit(0);
    }
}