package frc.robot.subsystems;

import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.MotionMagicConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.signals.AbsoluteSensorRangeValue;
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue;
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
    // private final PositionVoltage anglePID = new PositionVoltage(0).withSlot(0);
    final MotionMagicVoltage motionMagicPID = new MotionMagicVoltage(0);
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
        
        MotionMagicConfigs motionMagicConfigs = motorConfig.MotionMagic;
        motionMagicConfigs.MotionMagicCruiseVelocity = PivotConstants.MAGIC_CRUISE_VEL;
        motionMagicConfigs.MotionMagicAcceleration = PivotConstants.MAGIC_ACCEL; 
        motionMagicConfigs.MotionMagicJerk = PivotConstants.MAGIC_JERK;

        angleMotor.applyConfig(motorConfig);

        pivotTuner = new FalconTuner(angleMotor, "Pivot", this::setTargetAngle, targetAngle);

        initLogging();
    }

    private void initLogging() {
        LightningShuffleboard.setDoubleSupplier("Pivot", "Current Angle", () -> getAngle());
        LightningShuffleboard.setDoubleSupplier("Pivot", "Target Angle", () -> targetAngle);
        
        LightningShuffleboard.setBoolSupplier("Pivot", "On target", () -> onTarget());

        LightningShuffleboard.setDoubleSupplier("Pivot", "Bias", this::getBias);

        // LightningShuffleboard.setStringSupplier("Pivot", "Forward Limit", () -> angleMotor.getForwardLimit().getValue().toString());
        // LightningShuffleboard.setStringSupplier("Pivot", "Reverse Limit", () -> angleMotor.getReverseLimit().getValue().toString());

    }

    @Override
    public void periodic() {

        angleMotor.getConfig().Slot0.kP = LightningShuffleboard.getDouble("Pivot", "kP", PivotConstants.MOTOR_KP);
        angleMotor.getConfig().Slot0.kI = LightningShuffleboard.getDouble("Pivot", "kI", PivotConstants.MOTOR_KI);
        angleMotor.getConfig().Slot0.kD = LightningShuffleboard.getDouble("Pivot", "kD", PivotConstants.MOTOR_KD);
        angleMotor.getConfig().Slot0.kS = LightningShuffleboard.getDouble("Pivot", "kS", PivotConstants.MOTOR_KS);
        angleMotor.getConfig().Slot0.kV = LightningShuffleboard.getDouble("Pivot", "kV", PivotConstants.MOTOR_KV);
        angleMotor.getConfig().Slot0.kA = LightningShuffleboard.getDouble("Pivot", "kA", PivotConstants.MOTOR_KA);

        angleMotor.getConfig().MotionMagic.MotionMagicCruiseVelocity = LightningShuffleboard.getDouble("Pivot", "cruseVelocity", PivotConstants.MAGIC_CRUISE_VEL);
        angleMotor.getConfig().MotionMagic.MotionMagicAcceleration = LightningShuffleboard.getDouble("Pivot", "acceleration", PivotConstants.MAGIC_ACCEL);
        angleMotor.getConfig().MotionMagic.MotionMagicJerk = LightningShuffleboard.getDouble("Pivot", "jerk", PivotConstants.MAGIC_JERK);

        pivotTuner.update();
        
    }

    /**
     * Sets the angle of the pivot
     * 
     * @param angle Angle of the pivot
     */
    public void setTargetAngle(double angle) {
        MathUtil.clamp(angle + bias, PivotConstants.MIN_ANGLE, PivotConstants.MAX_ANGLE);
        targetAngle = angle;
        angleMotor.setControl(motionMagicPID.withPosition(angle));
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
}
