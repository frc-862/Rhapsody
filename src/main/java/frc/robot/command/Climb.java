// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.command;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.Climber;
import frc.thunder.shuffleboard.LightningShuffleboard;

public class Climb extends Command {
  /** Creates a new Climb. */

  // create vars
  private PIDController climbController = new PIDController(0.5, 0, 0); //TODO: tune pid
  private Climber climber;
  private double setPoint;

  public Climb(Climber climber, double setPoint) {
    // Use addRequirements() here to declare subsystem dependencies.

    // initialize variables
    this.climber = climber;
    this.setPoint = setPoint;
    addRequirements(climber);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    // use pid to set climber power
    double pidOutput = climbController.calculate(climber.getHeight(), setPoint);
    climber.setPower(pidOutput);
    
    LightningShuffleboard.setDouble("Climb", "Climb Height", climber.getHeight());
    LightningShuffleboard.setDouble("Climb", "Climb Power", pidOutput);

  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    climber.stopClimb();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }

  
}
