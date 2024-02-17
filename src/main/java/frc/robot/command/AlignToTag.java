// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.command;


import com.ctre.phoenix6.mechanisms.swerve.SwerveModule.DriveRequestType;

import javax.swing.plaf.TreeUI;

import com.ctre.phoenix6.mechanisms.swerve.SwerveRequest;
import com.ctre.phoenix6.mechanisms.swerve.SwerveRequest.FieldCentric;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.PathPlannerPath;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.subsystems.Limelights;
import frc.robot.subsystems.Swerve;
import frc.thunder.vision.Limelight;
import frc.robot.Constants.VisionConstants;
import frc.robot.command.MoveToPose;
import frc.robot.command.PointAtTag;



public class AlignToTag extends Command {
  
  public Pose2d target;
  public Swerve drivetrain;
  public MoveToPose moveToPose;
  public PointAtTag pointAtTag;

  private PathPlannerPath path;
  private Limelight limelight;
	private XboxController driver;
	private FieldCentric drive;
  private Limelights limelights;
	
	private int limelightPrevPipeline = 0;
  
  private boolean aligning;
  /** Creates a new AlignToTag. */
  public AlignToTag(Pose2d target, Swerve drivetrain, SwerveRequest.FieldCentric drive, XboxController driver) {
    // Use addRequirements() here to declare subsystem dependencies.
    this.target = target;
    this.drivetrain = drivetrain;
    this.drive = drive;
    this.driver = driver;
    //limelight = limelights.getStopMe();

		//limelightPrevPipeline = limelight.getPipeline();

		//limelight.setPipeline(VisionConstants.TAG_PIPELINE);
		
		//drive = new SwerveRequest.FieldCentric().withDriveRequestType(DriveRequestType.OpenLoopVoltage);//.withDeadband(DrivetrAinConstants.MaxSpeed * DrivetrAinConstants.SPEED_DB).withRotationalDeadband(DrivetrAinConstants.MaxAngularRate * DrivetrAinConstants.ROT_DB); // I want field-centric driving in closed loop
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    aligning = false;
    path = PathPlannerPath.fromPathFile("Amp autoalign");
    System.out.println("STARTED!!!!!!!11");
    // pointAtTag = new PointAtTag(drivetrain, limelights, driver);
    
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
      System.out.println("EXECUTINGINIGNIGN??????????????");
      System.out.println(aligning);
      //new Trigger(() -> (!(new MoveToPose(target, drivetrain, drive).isFinished())))
      //    .whileTrue(new MoveToPose(target, drivetrain, drive).schedule());
      //new Trigger(() -> (new MoveToPose(target, drivetrain, drive).isFinished()))
      //    .whileTrue(AutoBuilder.followPath(path));
      if (!(new MoveToPose(target, drivetrain, drive).isFinished())) {
        System.out.println("AHASIDHASHASID");
        new Trigger(() -> (true))).whileTrue(new MoveToPose(target, drivetrain, drive));
      } else if (!aligning && (new MoveToPose(target, drivetrain, drive).isFinished()) ) {
        AutoBuilder.followPath(path);
        aligning = true;
      }

  }
  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    drivetrain.setControl(drive.withVelocityX(0).withVelocityY(0));
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
