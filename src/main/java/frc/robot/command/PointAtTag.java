package frc.robot.command;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.ControllerConstants;
import frc.robot.Constants.VisionConstants;
import frc.robot.subsystems.Limelights;
import frc.robot.subsystems.Swerve;
import frc.thunder.shuffleboard.LightningShuffleboard;
import frc.thunder.vision.Limelight;

public class PointAtTag extends Command {

	private Swerve drivetrain;
	private Limelight limelight;
	private XboxController driver;
	
	private int limelightPrevPipeline = 0;
	private double pidOutput;
	private double targetHeading;
	private double previousTargetHeading;

	private PIDController headingController = VisionConstants.TAG_AIM_CONTROLLER;

	/**
	 * Creates a new PointAtTag.
	 * @param drivetrain to request movement 
	 * @param limelights to get the limelight from
	 * @param driver the driver's controller, used for drive input
	 */
	public PointAtTag(Swerve drivetrain, Limelights limelights, XboxController driver) {
		this.drivetrain = drivetrain;
		this.driver = driver;

		// TODO Figure out which of these is the right one to use
		limelight = limelights.getStopMe();

		if(limelight.getPipeline() != VisionConstants.SPEAKER_PIPELINE){
			limelight.setPipeline(VisionConstants.SPEAKER_PIPELINE);
		}

		addRequirements(drivetrain);
	}

	@Override
	public void initialize() {
		headingController.setTolerance(VisionConstants.ALIGNMENT_TOLERANCE);

		headingController.enableContinuousInput(-180, 180);

		initLogging();
	}

	private void initLogging() {
		LightningShuffleboard.setDoubleSupplier("PointAtTag", "Target Heading", () -> targetHeading);
		LightningShuffleboard.setDoubleSupplier("PointAtTag", "Pid Output", () -> pidOutput);
	}

	@Override
	public void execute() {
		previousTargetHeading = targetHeading;

		targetHeading = limelight.getTargetX();
		pidOutput = headingController.calculate(0, targetHeading);
		
		drivetrain.setFieldDriver(
			-MathUtil.applyDeadband(driver.getLeftY(), ControllerConstants.DEADBAND),
			-MathUtil.applyDeadband(driver.getLeftX(), ControllerConstants.DEADBAND),
			-pidOutput);
	}

	@Override
	public void end(boolean interrupted) {}

	/**
	 * Makes sure that the robot isn't jerking over to a different side while chasing pieces.
	 * @return t/f if the robot should trust the values
	 */
	public boolean trustValues(){
		if ((Math.abs(targetHeading) - Math.abs(previousTargetHeading)) < 6){
			return true;
		}
		return false;
	}

	@Override
	public boolean isFinished() {
		return false;
	}
}