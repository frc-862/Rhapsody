package frc.robot.command.shoot;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.PivotConstants;
import frc.robot.subsystems.Flywheel;
import frc.robot.subsystems.Pivot;
import frc.thunder.shuffleboard.LightningShuffleboard;

public class Tune extends Command {
	private final Flywheel flywheel;
	private final Pivot pivot;

	private double flywheelTargetRPMTOP = 0;
	private double flywheelTargetRPMBOTTOM = 0;
	private double pivotTargetAngle = PivotConstants.STOW_ANGLE;

	/**
	 * Creates a new PointBlankShot.
	 * 
	 * @param flywheel subsystem
	 * @param pivot subsystem
	 */
	public Tune(Flywheel flywheel, Pivot pivot) {
		this.flywheel = flywheel;
		this.pivot = pivot;

		addRequirements(pivot, flywheel);
	}

	@Override
	public void initialize() {}

	@Override
	public void execute() {
		flywheelTargetRPMTOP = LightningShuffleboard.getDouble("TUNE", "Target RPM TOP", flywheelTargetRPMTOP);
		flywheelTargetRPMBOTTOM = LightningShuffleboard.getDouble("TUNE", "Target RPM BOTTOM", flywheelTargetRPMBOTTOM);
		pivotTargetAngle = LightningShuffleboard.getDouble("TUNE", "Target Angle", pivotTargetAngle);
		LightningShuffleboard.setBool("TUNE", "Flywheel On target", flywheel.allMotorsOnTarget());
		LightningShuffleboard.setBool("TUNE", "Pivot On target", pivot.onTarget());

		// flywheel.setAllMotorsRPM(flywheelTargetRPM);
		flywheel.setTopMoterRPM(flywheelTargetRPMTOP);
		flywheel.setBottomMoterRPM(flywheelTargetRPMBOTTOM);
		pivot.setTargetAngle(pivotTargetAngle);
	}

	@Override
	public void end(boolean interrupted) {
		flywheel.coast(true);
		pivot.setTargetAngle(PivotConstants.STOW_ANGLE);
	}

	@Override
	public boolean isFinished() {
		return false;
	}
}
