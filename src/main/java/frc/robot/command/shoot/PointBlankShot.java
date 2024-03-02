package frc.robot.command.shoot;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.CandConstants;
import frc.robot.Constants.PivotConstants;
import frc.robot.subsystems.Pivot;
import frc.robot.subsystems.Flywheel;

public class PointBlankShot extends Command {

	private final Pivot pivot;
	private final Flywheel flywheel;

	/**
	 * Creates a new PointBlankShot.
	 * @param pivot subsystem
	 * @param flywheel subsystem
	 */
	public PointBlankShot(Pivot pivot, Flywheel flywheel) {
		this.pivot = pivot;
		this.flywheel = flywheel;

		addRequirements(pivot, flywheel);
	}

	@Override
	public void initialize() {
		pivot.setTargetAngle(CandConstants.POINT_BLANK_ANGLE + pivot.getBias());
		flywheel.setAllMotorsRPM(CandConstants.POINT_BLANK_RPM + flywheel.getBias());
	}

	@Override
	public void execute() {
		pivot.setTargetAngle(CandConstants.POINT_BLANK_ANGLE + flywheel.getBias());
		flywheel.setAllMotorsRPM(CandConstants.POINT_BLANK_RPM + pivot.getBias());
	}

	@Override
	public void end(boolean interrupted) {
		pivot.setTargetAngle(PivotConstants.STOW_ANGLE);
		flywheel.coast(true);
	}

	@Override
	public boolean isFinished() {
		return false;
	}
}