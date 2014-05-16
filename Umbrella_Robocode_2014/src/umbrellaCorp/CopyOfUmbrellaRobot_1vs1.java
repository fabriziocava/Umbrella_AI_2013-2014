package umbrellaCorp;
import java.awt.Color;

import robocode.*;
import robocode.util.Utils;
import static robocode.util.Utils.normalRelativeAngleDegrees;


public class CopyOfUmbrellaRobot_1vs1 extends AdvancedRobot {
	
	private static final double DOUBLE_PI = (Math.PI*2);
	private static final double HALF_PI = (Math.PI/2);
	private static final double WALL_AVOID_INTERVAL = 10;
	private static final double WALL_AVOID_FACTORS = 20;
	private static final double WALL_AVOID_DISTANCE = (WALL_AVOID_INTERVAL * WALL_AVOID_FACTORS);
	
	
	private int direction = 1;	
	private double idealDistance;
	private double enemyDistance = 0.0;
	private double enemyBearing = 0.0;
	
	public void run() {
		idealDistance = Math.max(getBattleFieldWidth()/2, getBattleFieldHeight()/2);
				
		setColors(Color.red,Color.white,Color.white);
		
//		turnRadarRightRadians(Double.POSITIVE_INFINITY);
		turnGunRightRadians(Double.POSITIVE_INFINITY);
		while(true) {
            // Always attempt to move straight ahead - the
            // adjustHeadingForWalls method will handle wall avoidance
            setTurnRightRadiansOptimal(adjustHeadingForWalls(0));
            scan();
            doMove();
            execute();
		}
	}
	
	
	/*
	 * EVENT
	 */
	
	public void onStatus(StatusEvent e) {

	}
	
	public void onCustomEvent(CustomEvent e) {
		
	}
	
	/*
	 * ON BULLET HIT
	 * is called when a bullet your robot fires, reaches a target.
	 */
	public void onBulletHit(BulletHitEvent e) {
		
	}
	
	/*
	 * ON BULLED MISSED
	 * is called when one of your bullets misses (hits a wall).
	 */
	public void onBulletMissed(BulletMissedEvent e) {
		
	}
	
	/*
	 * ON HIT BY BULLET
	 * is called when your robot is hit by bullet.
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		
	}
	
	/*
	 * ON SCANNED ROBOT
	 * is called when your robot scans another
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		double radarTurn = getHeadingRadians()+e.getBearingRadians()-getRadarHeadingRadians();
//		setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn));
		setTurnGunRightRadians(Utils.normalRelativeAngle(radarTurn));
		enemyDistance = e.getDistance();
		enemyBearing = e.getBearing();
	}

	/*
	 * ON HIT ROBOT
	 * is called when your robot crash another
	 */
	public void onHitRobot(HitRobotEvent e) {

	}
	
	/*
	 * ON HIT WALL
	 * is called when your robot crash with a wall
	 */
	public void onHitWall(HitWallEvent e) {
		
	}
	
	
	/*
	 * END_EVENT
	 */
	
	
	
	/*
	 * MOVEMENT
	 */
	
	public void goOnBorder() {
		if(getY()>getBattleFieldHeight()/2)
			ahead(getBattleFieldHeight()+getY());
		else
			ahead(getBattleFieldHeight()-getY());
	}
	
	public void move() {
		if(enemyDistance>=idealDistance) {
//			turnRight(enemyBearing);
//			ahead(Math.abs(idealDistance-enemyDistance));
		}
		
	}
	
	public void doMove() {
		if(getVelocity()==0)
			direction *= -1;
		setTurnRight(enemyBearing+90);
		setAhead(100*direction);
	}
	
	/*
	 * END_MOVEMENT
	 */
	
	
	/*
	 * UTILITY
	 */
	
	private double adjustHeadingForWalls(double heading) {
        double fieldHeight = getBattleFieldHeight();
        double fieldWidth = getBattleFieldWidth();
        double centerX = (fieldWidth / 2);
        double centerY = (fieldHeight / 2);
        
        double currentHeading = getRelativeHeadingRadians();
        double x = getX();
        double y = getY();
        
        boolean nearWall = false;
        
        double desiredX;
        double desiredY;
        
        if ((y < WALL_AVOID_DISTANCE) ||
                ((fieldHeight - y) < WALL_AVOID_DISTANCE)) {
            desiredY = centerY;
            nearWall = true;
        } else {
            desiredY = y;
        }
        if ((x < WALL_AVOID_DISTANCE) ||
                ((fieldWidth - x) < WALL_AVOID_DISTANCE)) {
            desiredX = centerX;
            nearWall = true;
        } else {
            desiredX = x;
        }
        if (nearWall) {
            double desiredBearing = calculateBearingToXYRadians(x,
                    y,
                    currentHeading,
                    desiredX,
                    desiredY);
            double distanceToWall = Math.min(
                    Math.min(x, (fieldWidth - x)),
                    Math.min(y, (fieldHeight - y)));
            int wallFactor =
                    (int)Math.min((distanceToWall / WALL_AVOID_INTERVAL),
                            WALL_AVOID_FACTORS);
            return ((((WALL_AVOID_FACTORS - wallFactor) * desiredBearing) +
                    (wallFactor * heading)) / WALL_AVOID_FACTORS);
        } else {
            return heading;
        }
	}
	
    public double getRelativeHeadingRadians() {
        double relativeHeading = getHeadingRadians();
        if (direction < 1) {
            relativeHeading =
                    normalizeAbsoluteAngleRadians(relativeHeading + Math.PI);
        }
        return relativeHeading;
    }
    
    public void reverseDirection() {
        double distance = (getDistanceRemaining() * direction);
        direction *= -1;
        setAhead(distance);
    }
    
    public void setAhead(double distance) {
        double relativeDistance = (distance * direction);
        super.setAhead(relativeDistance);
        // If distance is negative, reverse our direction
        if (distance < 0) {
            direction *= -1;
        }
    }
    
    public void setBack(double distance) {
        double relativeDistance = (distance * direction);
        super.setBack(relativeDistance);
        // If distance is positive, reverse our direction
        if (distance > 0) {
            direction *= -1;
        }
    }
    
    public void setTurnLeftRadiansOptimal(double angle) {
        double turn = normalizeRelativeAngleRadians(angle);
        if (Math.abs(turn) > HALF_PI) {
            reverseDirection();
            if (turn < 0) {
                turn = (HALF_PI + (turn % HALF_PI));
            } else if (turn > 0) {
                turn = -(HALF_PI - (turn % HALF_PI));
            }
        }
        setTurnLeftRadians(turn);
    }
    
    public void setTurnRightRadiansOptimal(double angle) {
        double turn = normalizeRelativeAngleRadians(angle);
        if (Math.abs(turn) > HALF_PI) {
            reverseDirection();
            if (turn < 0) {
                turn = (HALF_PI + (turn % HALF_PI));
            } else if (turn > 0) {
                turn = -(HALF_PI - (turn % HALF_PI));
            }
        }
        setTurnRightRadians(turn);
    }
    
    public double calculateBearingToXYRadians(double sourceX, double sourceY,
            double sourceHeading,
            double targetX, double targetY) {
        return normalizeRelativeAngleRadians(
                Math.atan2((targetX - sourceX), (targetY - sourceY)) -
                sourceHeading);
    }
    
    public double normalizeAbsoluteAngleRadians(double angle) {
        if (angle < 0) {
            return (DOUBLE_PI + (angle % DOUBLE_PI));
        } else {
            return (angle % DOUBLE_PI);
        }
    }
    
    public static double normalizeRelativeAngleRadians(double angle) {
        double trimmedAngle = (angle % DOUBLE_PI);
        if (trimmedAngle > Math.PI) {
            return -(Math.PI - (trimmedAngle % Math.PI));
        } else if (trimmedAngle < -Math.PI) {
            return (Math.PI + (trimmedAngle % Math.PI));
        } else {
            return trimmedAngle;
        }
    }
    
    
    
	
	public double calculatePower(double distance) {
		final double maxPower = 7;
		final double minDistance = 70;
		double currentEnergy = getEnergy();
		
		double power;
		
		if(distance<=minDistance) {
			power = maxPower;
		}
		else {
			power = maxPower*minDistance/distance;
		}
		if(currentEnergy<power)
			power = currentEnergy;
		return power;
	}
	
	
	/*
	 * END_UTILITY
	 */
	
}
