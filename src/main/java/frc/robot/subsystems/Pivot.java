package frc.robot.subsystems;

import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.signals.AbsoluteSensorRangeValue;
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.ForwardLimitValue;
import com.ctre.phoenix6.signals.ReverseLimitValue;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.RobotMap.CAN;
import frc.thunder.hardware.ThunderBird;
import frc.thunder.shuffleboard.LightningShuffleboard;
import frc.thunder.tuning.FalconTuner;
import frc.robot.Constants.PivotConstants;

public class Pivot extends SubsystemBase {

    private ThunderBird angleMotor;
    private CANcoder angleEncoder;
    private final PositionVoltage anglePID = new PositionVoltage(0).withSlot(0);
    private double bias = 0;

    private double targetAngle = 0;

    private FalconTuner pivotTuner;

    public Pivot() {
        CANcoderConfiguration angleConfig = new CANcoderConfiguration();
        angleConfig.MagnetSensor.withAbsoluteSensorRange(AbsoluteSensorRangeValue.Unsigned_0To1)
                .withMagnetOffset(PivotConstants.ENCODER_OFFSET)
                .withSensorDirection(PivotConstants.ENCODER_DIRECTION);

        angleEncoder = new CANcoder(CAN.PIVOT_ANGLE_CANCODER, CAN.CANBUS_FD);
        angleEncoder.getConfigurator().apply(angleConfig);

        angleMotor = new ThunderBird(CAN.PIVOT_ANGLE_MOTOR, CAN.CANBUS_FD, PivotConstants.MOTOR_INVERT,
                        PivotConstants.MOTOR_STATOR_CURRENT_LIMIT, PivotConstants.MOTOR_BRAKE_MODE);
        angleMotor.configPIDF(0, PivotConstants.MOTOR_KP, PivotConstants.MOTOR_KI,
                PivotConstants.MOTOR_KD, PivotConstants.MOTOR_KS, PivotConstants.MOTOR_KV);
        TalonFXConfiguration motorConfig = angleMotor.getConfig();

        motorConfig.Feedback.FeedbackRemoteSensorID = angleEncoder.getDeviceID();
        motorConfig.Feedback.FeedbackSensorSource = FeedbackSensorSourceValue.FusedCANcoder;
        motorConfig.Feedback.SensorToMechanismRatio = PivotConstants.ENCODER_TO_MECHANISM_RATIO;
        motorConfig.Feedback.RotorToSensorRatio = PivotConstants.ROTOR_TO_ENCODER_RATIO;

        angleMotor.applyConfig(motorConfig);

        pivotTuner = new FalconTuner(angleMotor, "Pivot", this::setTargetAngle, targetAngle);

        initLogging();
    }

    private void initLogging() {
        LightningShuffleboard.setDoubleSupplier("Pivot", "Current Angle", () -> getAngle());
        LightningShuffleboard.setDoubleSupplier("Pivot", "Target Angle", () -> targetAngle);

        LightningShuffleboard.setBoolSupplier("Pivot", "On target", () -> onTarget());

        LightningShuffleboard.setDoubleSupplier("Pivot", "Bias", this::getBias);

        LightningShuffleboard.setBoolSupplier("Pivot", "Forward Limit", () -> getForwardLimit());
        LightningShuffleboard.setBoolSupplier("Pivot", "Reverse Limit", () -> getReverseLimit());
    }

    @Override
    public void periodic() {
        pivotTuner.update();

        // SETS angle to angle of limit switch on press
        if (getForwardLimit()) {
            resetAngle(PivotConstants.MIN_ANGLE);
        } else if(getReverseLimit()) {
            resetAngle(PivotConstants.MAX_ANGLE);
        }
    }

    /**
     * Sets the angle of the pivot
     * @param angle Angle of the pivot
     */
    public void setTargetAngle(double angle) {
        MathUtil.clamp(angle + bias, PivotConstants.MIN_ANGLE, PivotConstants.MAX_ANGLE);
        targetAngle = angle;
        angleMotor.setControl(anglePID.withPosition(angle));
    }

    /**
     * @return The current angle of the pivot in degrees
     */
    public double getAngle() {
        return angleMotor.getPosition().getValue() * 360;
    }

    /**
     * @return Whether or not the pivot is on target, within
     *         PivotConstants.ANGLE_TOLERANCE
     */
    public boolean onTarget() {
        return Math.abs(getAngle() - targetAngle) < PivotConstants.ANGLE_TOLERANCE;
    }

    /**
     * Gets forward limit switch
     * @return true if pressed
     */
    public boolean getForwardLimit() {
        return angleMotor.getForwardLimit().refresh().getValue() == ForwardLimitValue.ClosedToGround;
    }

    /**
     * Gets reverse limit switch
     * @return true if pressed
     */
    public boolean getReverseLimit() {
        return angleMotor.getReverseLimit().refresh().getValue() == ReverseLimitValue.ClosedToGround;
    }

    /**
     * @return The bias to add to the target angle of the pivot
     */
    public double getBias() {
        return bias;
    }

    /**
     * Increases the bias of the pivot by set amount
     */
    public void increaseBias() {
        bias += PivotConstants.BIAS_INCREMENT;
    }

    /**
     * Decreases the bias of the pivot by set amount
     */
    public void decreaseBias() {
        bias -= PivotConstants.BIAS_INCREMENT;
    }

    /**
     * Resets the bias of the pivot
     */
    public void resetBias() {
        bias = 0;
    }

    /**
     * @param angle angle to set the pivot angle to
     */
    public void resetAngle(double angle) {
        // TODO is this necessary and implement
    }
}
