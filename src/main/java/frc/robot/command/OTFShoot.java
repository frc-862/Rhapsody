package frc.robot.command;

import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.mechanisms.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.mechanisms.swerve.SwerveRequest;
import com.ctre.phoenix6.mechanisms.swerve.SwerveRequest.FieldCentric;
import com.ctre.phoenix6.mechanisms.swerve.SwerveRequest.SwerveDriveBrake;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Telemetry;
import frc.robot.Constants.ControllerConstants;
import frc.robot.Constants.DrivetrainConstants;
import frc.robot.Constants.VisionConstants;
import frc.robot.subsystems.Limelights;
import frc.robot.subsystems.Swerve;
import frc.thunder.shuffleboard.LightningShuffleboard;
import frc.thunder.vision.Limelight;

public class OTFShoot extends Command {

	private Swerve drivetrain;
	private XboxController driver;

	private FieldCentric slow;
	private FieldCentric drive;
	
	private double pidOutput;

	private PIDController headingController = VisionConstants.TAG_AIM_CONTROLLER;

	/**
	 * Creates a new PointAtTag.
	 * @param drivetrain to request movement 
	 * @param driver the driver's controller, used for drive input
	 */
	public OTFShoot(Swerve drivetrain, XboxController driver) {

		this.drivetrain = drivetrain;
		this.driver = driver;
		
		drive = new SwerveRequest.FieldCentric().withDriveRequestType(DriveRequestType.OpenLoopVoltage);//.withDeadband(DrivetrAinConstants.MaxSpeed * DrivetrAinConstants.SPEED_DB).withRotationalDeadband(DrivetrAinConstants.MaxAngularRate * DrivetrAinConstants.ROT_DB); // I want field-centric driving in closed loop
		slow = new SwerveRequest.FieldCentric().withDriveRequestType(DriveRequestType.OpenLoopVoltage);
	}
	
	@Override
	public void initialize() {

		headingController.enableContinuousInput(-180, 180);

		addRequirements(drivetrain);
	}

	// Called every time the scheduler runs while the command is scheduled.
	@Override
	public void execute() {
	double spinupTime = 0.02;

	// Robot pose
	Pose2d pose = drivetrain.getPose().get();
    
    // Robot Acceleration
	double accelerationScaler = 0;
    double robotAccelerationX = drivetrain.getPigeon2().getAccelerationX().getValue();
    double robotAccelerationY = drivetrain.getPigeon2().getAccelerationY().getValue();

	// Robot velocity
	double veloctiyScaler = 1;
    double robotVelocityX = drivetrain.getCurrentRobotChassisSpeeds().vxMetersPerSecond;
    double robotVelocityY = drivetrain.getCurrentRobotChassisSpeeds().vyMetersPerSecond;

	double shotSpeed = 15;

    double distanceToSpeaker = Math.sqrt(Math.pow(
	VisionConstants.SPEAKER_LOCATION.getX() - pose.getX(), 2) 
	+ Math.pow(VisionConstants.SPEAKER_LOCATION.getY() - pose.getY(), 2) 
	+ Math.pow(VisionConstants.SPEAKER_LOCATION.getZ() - 0.75, 2));
    double timeToSpeaker = distanceToSpeaker / shotSpeed;
    
    // Velocity of the robot at release point
    double robotReleaseVelocityX = robotVelocityX * veloctiyScaler + (robotAccelerationX * accelerationScaler * spinupTime);
    double robotReleaseVelocityY = robotVelocityY * veloctiyScaler + (robotAccelerationY * accelerationScaler * spinupTime);
 
	// Offset of landing peice location bassed on robot velocity at release point
	double pieceDeltaX = robotReleaseVelocityX * timeToSpeaker;
	double pieceDeltaY = robotReleaseVelocityY * timeToSpeaker;

    // Change of target pose landing peice location bassed on robot velocity at release point
    double targetX = VisionConstants.SPEAKER_LOCATION.getX() + pieceDeltaX;
    double targetY = VisionConstants.SPEAKER_LOCATION.getY() + pieceDeltaY;

    //Change of final Robot pose due to acceleration and velocity
    double releaseRobotPoseX = pose.getX() + (robotVelocityX * spinupTime  * veloctiyScaler) + (0.5 * robotAccelerationX * spinupTime * spinupTime * accelerationScaler);
    double releaseRobotPoseY = pose.getY() + (robotVelocityY * spinupTime  * veloctiyScaler) + (0.5 * robotAccelerationY * spinupTime * spinupTime * accelerationScaler);

    // Get final Delta X and Delta Y to find the target heading of the robot
    double headingDeltaX = targetX - releaseRobotPoseX;
    double headingDeltaY = targetY - releaseRobotPoseY;

    //getting the angle to the target
    double targetHeading = Math.toDegrees(Math.atan2(headingDeltaY, headingDeltaX));

	// Heading of Robot without math and its delta
	var basicDeltaX = VisionConstants.SPEAKER_LOCATION.getX() - pose.getX();
	var basicDeltaY = VisionConstants.SPEAKER_LOCATION.getY() - pose.getY();
	double basicHeading = Math.toDegrees(Math.atan2(basicDeltaY, basicDeltaX));
	double basicDelta = Math.abs(targetHeading - basicHeading);
	
    pidOutput = headingController.calculate(pose.getRotation().getDegrees(), targetHeading);

    LightningShuffleboard.setDouble("OTF Shooting", "Distance to Speaker", distanceToSpeaker);
	LightningShuffleboard.setDouble("OTF Shooting", "Rotated Target Heading", (targetHeading + 360) % 360);
	LightningShuffleboard.setDouble("OTF Shooting", "Target Heading", targetHeading);
	LightningShuffleboard.setDouble("OTF Shooting", "Heading Delta X", headingDeltaX);
	LightningShuffleboard.setDouble("OTF Shooting", "Heading Delta Y", headingDeltaY);
	LightningShuffleboard.setDouble("OTF Shooting", "Robot X Velocity", robotVelocityX);	
	LightningShuffleboard.setDouble("OTF Shooting", "Robot Y Velocity", robotVelocityY);
    LightningShuffleboard.setDouble("OTF Shooting", "Robot X Accleration", robotAccelerationX);
    LightningShuffleboard.setDouble("OTF Shooting", "Robot Y Accleration", robotAccelerationY);
    LightningShuffleboard.setDouble("OTF Shooting", "Time to Speaker", timeToSpeaker);
    LightningShuffleboard.setDouble("OTF Shooting", "Release Robot X", releaseRobotPoseX);
    LightningShuffleboard.setDouble("OTF Shooting", "Release Robot Y", releaseRobotPoseY);
    LightningShuffleboard.setDouble("OTF Shooting", "Target X", targetX);
    LightningShuffleboard.setDouble("OTF Shooting", "Target Y", targetY);
    LightningShuffleboard.setDouble("OTF Shooting", "Impact Location Delta X", pieceDeltaX);
    LightningShuffleboard.setDouble("OTF Shooting", "Impact Location Delta Y", pieceDeltaY);
    LightningShuffleboard.setDouble("OTF Shooting", "PID Output", pidOutput);
    LightningShuffleboard.setDouble("OTF Shooting", "Robot X", pose.getX());
    LightningShuffleboard.setDouble("OTF Shooting", "Robot Y", pose.getY());
    LightningShuffleboard.setDouble("OTF Shooting", "Robot Heading", pose.getRotation().getDegrees());
	LightningShuffleboard.setDouble("OTF Shooting", "Basic Delta", basicDelta);
	LightningShuffleboard.setDouble("OTF Shooting", "Rotated Basic Heading", (basicHeading + 360) % 360);
	LightningShuffleboard.setDouble("OTF Shooting", "Basic Heading", basicHeading);
	LightningShuffleboard.setDouble("OTF Shooting", "Driver X", driver.getLeftX() * DrivetrainConstants.MaxSpeed);
	LightningShuffleboard.setDouble("OTF Shooting", "Driver Y", driver.getLeftY() * DrivetrainConstants.MaxSpeed);

	// drivetrain.applyRequest(() -> drive
	// 					.withVelocityX(-MathUtil.applyDeadband(driver.getLeftY(),
	// 							ControllerConstants.DEADBAND) * DrivetrainConstants.MaxSpeed) // Drive forward with negative Y (Its worth noting the field Y axis differs from the robot Y axis
	// 					.withVelocityY(-MathUtil.applyDeadband(driver.getLeftX(),
	// 							ControllerConstants.DEADBAND) * DrivetrainConstants.MaxSpeed) // Drive left with negative X (left)
	// 					.withRotationalRate(-MathUtil.applyDeadband(driver.getRightX(),
	// 							ControllerConstants.DEADBAND) * DrivetrainConstants.MaxAngularRate
	// 							* DrivetrainConstants.ROT_MULT) // Drive counterclockwise with negative X (left)
	// 			);
	if (driver.getRightBumper()) {
		drivetrain.setControl(slow.withVelocityX(-MathUtil.applyDeadband(driver.getLeftY(), ControllerConstants.DEADBAND) * DrivetrainConstants.MaxSpeed * DrivetrainConstants.SLOW_SPEED_MULT) // Drive forward with negative Y (Its worth noting the field Y axis differs from the robot Y axis_
			.withVelocityY(-MathUtil.applyDeadband(driver.getLeftX(), ControllerConstants.DEADBAND) * DrivetrainConstants.MaxSpeed * DrivetrainConstants.SLOW_SPEED_MULT) // Drive left with negative X (left)
			.withRotationalRate(pidOutput) // Drive counterclockwise with negative X (left)
		);
	} else {
		drivetrain.setControl(drive.withVelocityX(-MathUtil.applyDeadband(driver.getLeftY(), ControllerConstants.DEADBAND) * DrivetrainConstants.MaxSpeed) // Drive forward with negative Y (Its worth noting the field Y axis differs from the robot Y axis_
			.withVelocityY(-MathUtil.applyDeadband(driver.getLeftX(), ControllerConstants.DEADBAND) * DrivetrainConstants.MaxSpeed) // Drive left with negative X (left)
			.withRotationalRate(pidOutput) // Rotate toward the desired direction
		); // Drive counterclockwise with negative X (left)
	}
	}

	@Override
	public void end(boolean interrupted) {}

	@Override
	public boolean isFinished() {
		return false;
	}
}