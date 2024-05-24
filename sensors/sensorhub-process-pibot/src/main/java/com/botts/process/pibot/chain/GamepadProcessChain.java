package com.botts.process.pibot.chain;

import net.opengis.swe.v20.DataRecord;
import net.opengis.swe.v20.DataType;
import net.opengis.swe.v20.Quantity;
import org.sensorhub.api.processing.OSHProcessInfo;
import org.vast.data.DataBlockList;
import org.vast.data.DataBlockMixed;
import org.vast.data.DataBlockParallel;
import org.vast.process.ExecutableProcessImpl;
import org.vast.process.ProcessException;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;

import java.util.Objects;

public class GamepadProcessChain extends ExecutableProcessImpl {
    public static final OSHProcessInfo INFO = new OSHProcessInfo("gamepadchain", "Gamepad Process Chain", null, GamepadProcessChain.class);

    // Inputs
    Object lock = new Object();
    // Can use index or each gamepad has isPrimaryController value
//    private Count primaryControllerIndexInput;
    private DataRecord gamepadRecordInput;

    // Outputs
    private Quantity dpadOutput;
    private Quantity xAxisOutput;
    private Quantity yAxisOutput;
    private Quantity leftOutput;
    private Quantity rightOutput;
    private Quantity aOutput;
    private Quantity bOutput;

    // vars
    int numGamepads;
    float povValue;
    float xValue;
    float yValue;
    float leftValue;
    float rightValue;
    float aValue;
    float bValue;
    float oneValue;
    float twoValue;

    public GamepadProcessChain() {
        super(INFO);

        SWEHelper sweHelper = new SWEHelper();

        inputData.add("gamepadRecord", gamepadRecordInput = createGamepadRecord());

        outputData.add("pov", dpadOutput = sweHelper.createQuantity()
                .label("Hat Switch")
                .uomUri(SWEConstants.UOM_UNITLESS)
                .build());
        outputData.add("x", xAxisOutput = sweHelper.createQuantity()
                .label("x")
                .uomUri(SWEConstants.UOM_UNITLESS)
                .build());
        outputData.add("y", yAxisOutput = sweHelper.createQuantity()
                .label("y")
                .uomUri(SWEConstants.UOM_UNITLESS)
                .build());
        outputData.add("LeftThumb", leftOutput = sweHelper.createQuantity()
                .label("Left Thumb")
                .uomUri(SWEConstants.UOM_UNITLESS)
                .build());
        outputData.add("RightThumb", rightOutput = sweHelper.createQuantity()
                .label("Right Thumb")
                .uomUri(SWEConstants.UOM_UNITLESS)
                .build());
        outputData.add("A", aOutput = sweHelper.createQuantity()
                .label("A")
                .uomUri(SWEConstants.UOM_UNITLESS)
                .build());
        outputData.add("B", bOutput = sweHelper.createQuantity()
                .label("B")
                .uomUri(SWEConstants.UOM_UNITLESS)
                .build());
//        outputData.add("1", aOutput = sweHelper.createQuantity()
//                .label("1")
//                .uomUri(SWEConstants.UOM_UNITLESS)
//                .build());
//        outputData.add("2", bOutput = sweHelper.createQuantity()
//                .label("2")
//                .uomUri(SWEConstants.UOM_UNITLESS)
//                .build());
    }

    public DataRecord createGamepadRecord() {
        SWEHelper sweFactory = new SWEHelper();
        return sweFactory.createRecord()
                .name("gamepadRecord")
                .label("Gamepad Output Record")
                .definition(SWEHelper.getPropertyUri("GamepadOutputRecord"))
                .addField("primaryControlStreamIndex", sweFactory.createCount()
                        .label("Primary Control Stream Index")
                        .description("Index of the primary control stream")
                        .definition(SWEHelper.getPropertyUri("PrimaryControlStreamIndex")))
                .addField("numControlStreams", sweFactory.createCount()
                        .label("Num Control Streams")
                        .description("Number of Control Streams")
                        .definition(SWEHelper.getPropertyUri("NumControlStreams")))
                .addField("numGamepads", sweFactory.createCount()
                        .name("numGamepads")
                        .label("Num Gamepads")
                        .description("Number of connected gamepads")
                        .definition(SWEHelper.getPropertyUri("GamepadCount"))
                        .id("numGamepads").build())
                .addField("gamepads", sweFactory.createArray()
                        .name("gamepads")
                        .label("Gamepads")
                        .description("List of connected gamepads.")
                        .definition(SWEHelper.getPropertyUri("GamepadArray"))
                        .withVariableSize("numGamepads")
                        .withElement("gamepad", sweFactory.createRecord()
                                .label("Gamepad")
                                .description("Gamepad Data")
                                .definition(SWEHelper.getPropertyUri("Gamepad"))
                                .addField("gamepadName", sweFactory.createText()
                                        .label("Gamepad Name")
                                        .definition("GamepadName"))
                                .addField("isPrimaryController", sweFactory.createBoolean()
                                        .label("Is Primary Controller")
                                        .definition(SWEHelper.getPropertyUri("IsPrimaryController"))
                                        .value(false))
                                .addField("numComponents", sweFactory.createCount()
                                        .label("Num Components")
                                        .description("Number of button and axis components on gamepad")
                                        .definition(SWEHelper.getPropertyUri("NumGamepadComponents"))
                                        .id("numComponents")
                                        .build())
                                .addField("gamepadComponents", sweFactory.createArray()
                                        .name("gamepadComponents")
                                        .label("Gamepad Components")
                                        .description("Data of Connected Gamepad Components")
                                        .definition(SWEHelper.getPropertyUri("GamepadComponentArray"))
                                        .withVariableSize("numComponents")
                                        .withElement("component", sweFactory.createRecord()
                                                .name("component")
                                                .label("Component")
                                                .description("Gamepad Component (A button, B button, X axis, etc.)")
                                                .definition(SWEHelper.getPropertyUri("GamepadComponent"))
                                                .addField("componentName", sweFactory.createText()
                                                        .label("Component Name")
                                                        .description("Name of component")
                                                        .definition(SWEHelper.getPropertyUri("ComponentName"))
                                                        .value(""))
                                                .addField("componentValue", sweFactory.createQuantity()
                                                        .label("Component Value")
                                                        .description("Value of component")
                                                        .definition(SWEHelper.getPropertyUri("ComponentValue"))
                                                        .dataType(DataType.FLOAT)
                                                        .value(0.0f)
                                                        .addAllowedInterval(-1.0f, 1.0f)))
                                )).build()).build();
    }

    @Override
    public void init() throws ProcessException {
        super.init();
    }

    @Override
    public void execute() throws ProcessException {
        try {
            // Prevents concurrent modification exception
            synchronized (lock) {
                numGamepads = gamepadRecordInput.getComponent("numGamepads").getData().getIntValue();

                povValue = 0.0f;
                xValue = 0.0f;
                yValue = 0.0f;
                leftValue = 0.0f;
                rightValue = 0.0f;
//                oneValue = 0.0f;
//                twoValue = 0.0f;
                // parse gamepads, extract values for pov, x, y, LT, RT, set output values to primary controller's values

                if (numGamepads > 0) {
                    for (int gamepadIndex = 0; gamepadIndex < numGamepads; gamepadIndex++) {
                        // Get gamepad by index
                        DataBlockMixed gamepad = (DataBlockMixed) ((DataBlockList) gamepadRecordInput.getComponent("gamepads").getData()).get(gamepadIndex);

                        // Get data such as isPrimaryController, number of components, and the array of components
                        boolean isPrimaryController = gamepad.getBooleanValue(1);
                        int numComponents = gamepad.getIntValue(2);
                        DataBlockParallel componentArray = (DataBlockParallel) gamepad.getUnderlyingObject()[3];

                        // Only check if primary controller
                        if (isPrimaryController) {

                            // Check each component to see if active
                            for (int j = 0; j < numComponents; j++) {
                                String[] componentNames = (String[]) componentArray.getUnderlyingObject()[0].getUnderlyingObject();
                                double[] componentValues = (double[]) componentArray.getUnderlyingObject()[1].getUnderlyingObject();

                                if (Objects.equals(componentNames[j], "pov")) {
                                    povValue = (float) componentValues[j];
                                }
                                if (Objects.equals(componentNames[j], "x")) {
                                    xValue = (float) componentValues[j];
                                }
                                if (Objects.equals(componentNames[j], "y")) {
                                    yValue = (float) componentValues[j];
                                }
                                if (Objects.equals(componentNames[j], "LeftThumb")) {
                                    leftValue = (float) componentValues[j];
                                }
                                if (Objects.equals(componentNames[j], "RightThumb")) {
                                    rightValue = (float) componentValues[j];
                                }
                                if (Objects.equals(componentNames[j], "A")) {
                                    aValue = (float) componentValues[j];
                                }
                                if (Objects.equals(componentNames[j], "B")) {
                                    bValue = (float) componentValues[j];
                                }
//                                if (Objects.equals(componentNames[j], "1")) {
//                                    oneValue = (float) componentValues[j];
//                                }
//                                if (Objects.equals(componentNames[j], "2")) {
//                                    twoValue = (float) componentValues[j];
//                                }
                            }
                        }
                    }

                }

                // Set process outputs
                dpadOutput.getData().setFloatValue(povValue);
                xAxisOutput.getData().setFloatValue(xValue);
                yAxisOutput.getData().setFloatValue(yValue);
                leftOutput.getData().setFloatValue(leftValue);
                rightOutput.getData().setFloatValue(rightValue);
                aOutput.getData().setFloatValue(aValue);
                bOutput.getData().setFloatValue(bValue);
//                oneOutput.getData().setFloatValue(oneValue);
//                twoOutput.getData().setFloatValue(twoValue);
            }
        } catch (Exception e) {
            reportError(e.getMessage());
            reportError("Error retrieving gamepad data");
        }
    }
}
