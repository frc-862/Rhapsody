package frc.robot.subsystems;

import com.ctre.phoenix6.hardware.TalonFX;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.IndexerConstants;
import frc.robot.Constants.RobotMap.CAN;
import frc.robot.Constants.RobotMap.DIO;
import frc.thunder.config.FalconConfig;

public class Indexer extends SubsystemBase {
    private TalonFX indexerMotor;
    private DigitalInput indexerSensorEntry = new DigitalInput(DIO.INDEXER_BEAMBREAK);
    private DigitalInput indexerSensorExit = new DigitalInput(DIO.INDEXER_BEAMBREAK);

    public Indexer() {
        indexerMotor = FalconConfig.createMotor(CAN.INDEXER_MOTOR, CAN.CANBUS_FD,
                IndexerConstants.INDEXER_MOTOR_INVERTED,
                IndexerConstants.INDEXER_MOTOR_SUPPLY_CURRENT_LIMIT,
                IndexerConstants.INDEXER_MOTOR_STATOR_CURRENT_LIMIT,
                IndexerConstants.INDEXER_MOTOR_NEUTRAL_MODE);
    }

    public void setPower(double power) {
        indexerMotor.set(power);
    }

    public void indexIn() {
        indexerMotor.set(IndexerConstants.INDEXER_DEFAULT_POWER);
    }

    public void indexOut() {
        indexerMotor.set(-IndexerConstants.INDEXER_DEFAULT_POWER);
    }

    /**
     * STOP IT GET SOME HELP
     */
    public void stop() {
        indexerMotor.set(0d);
    }

    /**
     * 
     * @return entry beambreak state
     */
    public boolean getEntryBeamBreakState() {
        return !indexerSensorEntry.get();
    }

    public boolean getExitBeamBreakState() {
        return !indexerSensorExit.get();
    }
     /**
      * 
      * @return exit beambreak state
      */
    
    public boolean hasShot() {
        return false; // TODO add actual logic
    }
}
