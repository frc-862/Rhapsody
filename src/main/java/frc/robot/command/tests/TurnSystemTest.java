// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.command.tests;

import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import frc.robot.command.tests.testcmds.TurnTest;
import frc.robot.subsystems.Swerve;
import frc.thunder.command.TimedCommand;
import frc.thunder.testing.SystemTestCommandGroup;

public class TurnSystemTest extends SystemTestCommandGroup {

    public TurnSystemTest(Swerve drivetrain, double speed) {
        super(
                new SequentialCommandGroup(
                        new WaitCommand(0.5),
                        new TimedCommand(new TurnTest(drivetrain, () -> speed), 1), // Rotates at speed 10% max speed
                                                                                    // for 3 second
                        // new WaitCommand(1),
                        // new TimedCommand(new TurnTest(drivetrain, () -> -speed), 3), // Rotates in
                        // the opposite dir for 3 second
                        // new WaitCommand(0.5),
                        new InstantCommand(() -> drivetrain.brake()) // Brakes the robot, no need imo cuz it should
                                                                     // break after the TurnTest command

                ));
    }
}
