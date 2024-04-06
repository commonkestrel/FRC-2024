package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.IntakeConstants;
import frc.robot.Constants.ShooterConstants;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;

public class ShootSpeakerCommand extends Command {
    private enum State {
        Feed,
        RevUp,
        Shoot,
        Wait,
    }

    private final Intake m_intake;
    private final Shooter m_shooter;
    
    private State m_currentState;
    private long m_lastDetected;

    public ShootSpeakerCommand(Intake intake, Shooter shooter) {
        m_intake = intake;
        m_shooter = shooter;
        addRequirements(m_intake, m_shooter);

        m_currentState = State.Feed;
    }

    @Override
    public void initialize() {
        if (!m_intake.noteDetected()) {
            m_currentState = State.RevUp;
            m_intake.setTargetIndexerVelocity(IntakeConstants.idleIndexerTarget);
            m_intake.setTargetPrerollerVelocity(IntakeConstants.idlePrerollerTarget);
        }
        m_currentState = State.Feed;
        m_shooter.setTargetVelocity(ShooterConstants.speakerTarget);
    }

    @Override
    public void execute() {
        System.out.print("Shooter: "); System.out.println(m_shooter.getVelocity());
        System.out.print("Intake: "); System.out.println(m_intake.getIndexerVelocity());
        switch (m_currentState) {
        case Feed:
            if (m_intake.noteDetected()) {
                m_intake.stop();
                m_currentState = State.RevUp;
            }
            break;
        case RevUp:
            if (m_shooter.getVelocity() >= ShooterConstants.speakerTarget) {
                m_intake.setIndexerSpeed(1.0);
                m_currentState = State.Shoot;
            }
            break;
        case Shoot:
            if (!m_intake.noteDetected()) {
                m_lastDetected = System.nanoTime();
                m_currentState = State.Wait;
            }
            break;
        case Wait:
            break;
        }
    }

    @Override
    public void end(boolean interrupted) {
        m_intake.stop();
        m_shooter.setSpeed(0.0);
    }

    @Override
    public boolean isFinished() {
        // Wait one second after the note is last detected before ending
        return m_currentState == State.Wait && System.nanoTime() - m_lastDetected >= 1e9;
    }
}
