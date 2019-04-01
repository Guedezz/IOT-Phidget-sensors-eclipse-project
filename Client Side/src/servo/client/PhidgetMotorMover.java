package servo.client;

import com.phidget22.PhidgetException;
import com.phidget22.RCServo;
import com.phidget22.RCServoPositionChangeEvent;
import com.phidget22.RCServoPositionChangeListener;
import com.phidget22.RCServoTargetPositionReachedEvent;
import com.phidget22.RCServoTargetPositionReachedListener;
import com.phidget22.RCServoVelocityChangeEvent;
import com.phidget22.RCServoVelocityChangeListener;

public class PhidgetMotorMover {
	// Singleton implementation to allow multiple callbacks to the code
	static RCServo servo = null;

	public static RCServo getInstance() {
		System.out.println("\nIn singleton constructor");
		if (servo == null) {
			servo = PhidgetMotorMover();
		}
		return servo;
	}

	private static RCServo PhidgetMotorMover() {
		// Create new instance of servo board and start listening for motor changes
		// This method should only be called once when first constructing a servo
		// instance
		try {
			System.out.println("Constructing MotorMover");
			servo = new RCServo();

			servo.addTargetPositionReachedListener(new RCServoTargetPositionReachedListener() {
				public void onTargetPositionReached(RCServoTargetPositionReachedEvent e) {
					System.out.println("\nTarget Position Reached: " + e.getPosition());
				}
			});

			servo.open(2000);
		} catch (PhidgetException e) {
			e.getMessage();
		}
		return servo;
	}

	public static void moveServoTo(double motorPosition) throws PhidgetException {
		// Get the servo that is available
		PhidgetMotorMover.getInstance();
		servo.setMaxPosition(210.0);
		servo.setTargetPosition(motorPosition);
		System.out.println("Moving motor to position " + motorPosition);
		servo.setEngaged(true);
	}

}
