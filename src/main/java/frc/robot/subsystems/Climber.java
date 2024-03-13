package frc.robot.subsystems;

import com.ctre.phoenix6.configs.FeedbackConfigs;
import com.ctre.phoenix6.configs.SoftwareLimitSwitchConfigs;
import com.ctre.phoenix6.controls.DutyCycleOut;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.hardware.core.CoreTalonFX;
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.ForwardLimitValue;
import com.ctre.phoenix6.signals.ReverseLimitValue;

import edu.wpi.first.wpilibj.motorcontrol.Talon;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ClimbConstants;
import frc.robot.Constants.MercuryPivotConstants;
import frc.robot.Constants.RobotMap.CAN;
import frc.thunder.hardware.ThunderBird;
import frc.thunder.shuffleboard.LightningShuffleboard;

public class Climber extends SubsystemBase {

    private ThunderBird climbMotorR;
    private ThunderBird climbMotorL;

    private PositionVoltage setPointControlR = new PositionVoltage(0d);
    private PositionVoltage setPointControlL = new PositionVoltage(0d);

    private DutyCycleOut manualControl = new DutyCycleOut(0d);

    public Climber() {
        // configure climb motors
        climbMotorR = new ThunderBird(CAN.CLIMB_RIGHT, CAN.CANBUS_FD,
                ClimbConstants.CLIMB_RIGHT_MOTOR_INVERT, ClimbConstants.CLIMB_MOTOR_STATOR_CURRENT_LIMIT, ClimbConstants.CLIMB_MOTOR_BRAKE_MODE);
        climbMotorL = new ThunderBird(CAN.CLIMB_LEFT, CAN.CANBUS_FD,
            ClimbConstants.CLIMB_LEFT_MOTOR_INVERT, ClimbConstants.CLIMB_MOTOR_STATOR_CURRENT_LIMIT, ClimbConstants.CLIMB_MOTOR_BRAKE_MODE);

        climbMotorL.configPIDF(0, ClimbConstants.UNLOADED_KP, ClimbConstants.UNLOADED_KI, ClimbConstants.UNLOADED_KD);
        climbMotorL.configPIDF(1, ClimbConstants.LOADED_KP, ClimbConstants.LOADED_KI, ClimbConstants.LOADED_KD);

        climbMotorR.configPIDF(0, ClimbConstants.UNLOADED_KP, ClimbConstants.UNLOADED_KI, ClimbConstants.UNLOADED_KD);
        climbMotorR.configPIDF(1, ClimbConstants.LOADED_KP, ClimbConstants.LOADED_KI, ClimbConstants.LOADED_KD);

        FeedbackConfigs sensorConf = new FeedbackConfigs();
        SoftwareLimitSwitchConfigs softLimitConf = new SoftwareLimitSwitchConfigs();

        sensorConf.FeedbackSensorSource = FeedbackSensorSourceValue.RotorSensor;
        sensorConf.SensorToMechanismRatio = ClimbConstants.GEAR_REDUCTION;

        softLimitConf.ForwardSoftLimitEnable = true;
        softLimitConf.ForwardSoftLimitThreshold = ClimbConstants.MAX_HEIGHT;

        climbMotorL.applyConfig(climbMotorL.getConfig().withFeedback(sensorConf).withSoftwareLimitSwitch(softLimitConf));
        climbMotorR.applyConfig(climbMotorR.getConfig().withFeedback(sensorConf).withSoftwareLimitSwitch(softLimitConf));

        initLogging();

        climbMotorL.setPosition(0d);
        climbMotorR.setPosition(0d);
    }

    private void initLogging() {
        LightningShuffleboard.setDoubleSupplier("Climb", "Left Height", () -> getHeightL());
        LightningShuffleboard.setDoubleSupplier("Climb", "Right Height", () -> getHeightR());
        LightningShuffleboard.setDoubleSupplier("Climb", "Left Setpoint", () -> getSetpointL());
        LightningShuffleboard.setDoubleSupplier("Climb", "Right Setpoint", () -> getSetpointR());
        LightningShuffleboard.setDoubleSupplier("Climb", "Left Applied", () -> climbMotorL.getMotorVoltage().getValueAsDouble());
        LightningShuffleboard.setDoubleSupplier("Climb", "Right Applied", () -> climbMotorR.getMotorVoltage().getValueAsDouble());
        }

    /**
     * Sets power to both climb motors
     * @param power the power to set both climb motors to
     */
    public void setPower(double power) {
        setPower(power, power);
    }

    /**
     * Sets power to climb motors
     * @param powerR the power to set the right climb motor to
     * @param powerL the power to set the left climb motor to
     */
    public void setPower(double powerL, double powerR) {
        climbMotorR.setControl(manualControl.withOutput(powerR)); // FOC On by default
        climbMotorL.setControl(manualControl.withOutput(powerL));
    }

    /**
     * sets the setpoint of both climb motors
     * @param setPoint setpoint for both climb motors in pulley rotations
     */
    public void setSetpoint(double setPoint) {
        setSetpoint(setPoint, setPoint);
    }

    /**
     * sets the setpoint of the climb motors
     * @param leftSetPoint setpoint for left climb motor in pulley rotations
     * @param rightSetPoint setpoint for right climb motor in pulley rotations
     */
    public void setSetpoint(double leftSetPoint, double rightSetPoint) {
        setPointControlL = setPointControlL.withPosition(leftSetPoint);
        setPointControlR = setPointControlR.withPosition(rightSetPoint);
        climbMotorL.setControl(setPointControlL);
        climbMotorR.setControl(setPointControlR);
    }

    /**
     * sets the setpoint of the climb motors to the max height
     */
    public void deploy() {
        setSetpoint(ClimbConstants.MAX_HEIGHT);
    }

    /**
     * sets the setpoint of the climb motors to the retracted position
     */
    public void retract() {
        setSetpoint(ClimbConstants.CLIMB_PID_SETPOINT_RETRACTED);
    }

    /**
     * Stops all climb motors
     */
    public void stop() {
        setPower(0d);
    }

    public boolean getForwardLimit(TalonFX climbMotor) {
        return climbMotor.getForwardLimit().refresh().getValue() == ForwardLimitValue.ClosedToGround;
    }

    public boolean getReverseLimit(TalonFX climbMotor) {
        return climbMotor.getReverseLimit().refresh().getValue() == ReverseLimitValue.ClosedToGround;
    }


    /**
     * @return height of right climb arm
     */
    public double getHeightR() {
        return climbMotorR.getPosition().getValueAsDouble();
    }

    /**
     * @return height of left climb arm
     */
    public double getHeightL() {
        return climbMotorL.getPosition().getValueAsDouble();
    }

    /**
     * @return the setpoint of the right climb arm
     */
    public double getSetpointR() {
        return this.setPointControlR.Position;
    }

    /**
     * @return the setpoint of the left climb arm
     */
    public double getSetpointL() {
        return this.setPointControlL.Position;
    }

    @Override
    public void periodic() {
        // zeroes height if the limit switch is pressed or position is negative
        for (TalonFX motor : new TalonFX[] {climbMotorR, climbMotorL}) {
            if (motor.getPosition().getValueAsDouble() < 0
                    || getReverseLimit(motor)) {
                motor.setPosition(0d);
            }
            
            if (getForwardLimit(motor)) {
                if (motor == climbMotorR) {
                    setSetpoint(ClimbConstants.MAX_HEIGHT);
                } else {
                    setSetpoint(ClimbConstants.MAX_HEIGHT);
                }
            }
        }
    }
    /**
     * Gets forward limit switch
     * 
     * @return true if pressed
     */
    public boolean getForwardLimitLeft() {
        return climbMotorL.getForwardLimit().refresh().getValue() == ForwardLimitValue.ClosedToGround;
    }
    /**
     * @return true if pressed
     */
    public boolean getForwardLimitRight() {
        return climbMotorR.getForwardLimit().refresh().getValue() == ForwardLimitValue.ClosedToGround;
    }
    /**
     * Gets reverse limit switch
     * 
     * @return true if pressed
     */
    public boolean getReverseLimitLeft() {
        return climbMotorL.getReverseLimit().refresh().getValue() == ReverseLimitValue.ClosedToGround;
    }
    /**
     * @return true if pressed
     */
    public boolean getReverseLimitRight() {
        return climbMotorR.getReverseLimit().refresh().getValue() == ReverseLimitValue.ClosedToGround;
    }
}
