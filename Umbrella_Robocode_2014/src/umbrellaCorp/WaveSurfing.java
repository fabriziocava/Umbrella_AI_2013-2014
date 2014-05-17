package umbrellaCorp;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

class EnemyWave {
    Point2D.Double fireLocation;
    long fireTime;
    double bulletVelocity, directAngle, distanceTraveled;
    int direction;

    public EnemyWave() { 
    	
    }
}

public class WaveSurfing {
	
	private MadRobot mr;
	public static int BINS = 47;
	public static double surf_stats[] = new double[BINS];
	public Point2D.Double myLocation;
	public Point2D.Double enemyLocation;
	
	public ArrayList<EnemyWave> enemyWaves;
	public ArrayList<Integer> surfDirections;
	public ArrayList<Double> surfAbsBearings;
	
	public static double oppEnergy = 100.0;
	
	public static Rectangle2D.Double battlefieldRect;
	public static double WALL_STICK = 160;

	
	public WaveSurfing(MadRobot mr) {
		enemyWaves = new ArrayList<EnemyWave>();
		surfDirections = new ArrayList<Integer>();
		surfAbsBearings = new ArrayList<Double>();
		this.mr=mr;
	}
	
	public void init() {
		battlefieldRect = new java.awt.geom.Rectangle2D.Double(18, 18, mr.getBattleFieldWidth()-36, mr.getBattleFieldHeight()-36);
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		myLocation = new Point2D.Double(mr.getX(), mr.getY());
		double lateralVelocity = mr.getVelocity()*Math.sin(e.getBearingRadians());
		double absBearings = e.getBearingRadians() + mr.getHeadingRadians();
		
		mr.setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearings-mr.getRadarHeadingRadians())*2);
		
		surfDirections.add(0, new Integer((lateralVelocity>=0)?1:-1));
		surfAbsBearings.add(0, new Double(absBearings+Math.PI));
		
		double bulletPower = oppEnergy-e.getEnergy();
		if(bulletPower<=3 && bulletPower>=0.1 && surfDirections.size()>=2) {
			EnemyWave ew = new EnemyWave();
			ew.fireTime = mr.getTime()-1;
			ew.bulletVelocity = Util.bulletVelocity(bulletPower);
			ew.distanceTraveled = Util.bulletVelocity(bulletPower);
			ew.direction = ((Integer) surfDirections.get(2)).intValue();
			ew.directAngle = ((Double) surfAbsBearings.get(2)).doubleValue();
			ew.fireLocation = (Point2D.Double) enemyLocation.clone();
			enemyWaves.add(ew);
		}
		
		oppEnergy = e.getEnergy();
		
		enemyLocation = Util.project(myLocation, absBearings, e.getDistance());
		
		updateWaves();
		doSurfing();		
	}
	
	public void updateWaves() {
		for (int x=0; x<enemyWaves.size(); x++) {
			EnemyWave ew = (EnemyWave) enemyWaves.get(x);
			ew.distanceTraveled = (mr.getTime()-ew.fireTime)*ew.bulletVelocity;
			if (ew.distanceTraveled>myLocation.distance(ew.fireLocation)+50) {
				enemyWaves.remove(x);
				x--;
			}
		}
	}

	public EnemyWave getClosestSurfableWave() {
		double closestDistance = 50000;
		EnemyWave surfWave = null;

		for (int x=0; x<enemyWaves.size(); x++) {
			EnemyWave ew = (EnemyWave) enemyWaves.get(x);
			double distance = myLocation.distance(ew.fireLocation) - ew.distanceTraveled;
			if (distance>ew.bulletVelocity && distance<closestDistance) {
				surfWave = ew;
				closestDistance = distance;
			}
		}
		return surfWave;
	}


	public static int getFactorIndex(EnemyWave ew, Point2D.Double targetLocation) {
		double offsetAngle = (Util.absoluteBearing(ew.fireLocation, targetLocation) - ew.directAngle);
		double factor = Utils.normalRelativeAngle(offsetAngle) / Util.maxEscapeAngle(ew.bulletVelocity) * ew.direction;
		return ((int) Util.limit(0, (factor*((BINS-1)/2))+((BINS-1)/2), BINS-1));
	}

	public void logHit(EnemyWave ew, Point2D.Double targetLocation) {
		int index = getFactorIndex(ew, targetLocation);

		for (int x=0; x<BINS; x++) {
			surf_stats[x] += 1.0 / (Math.pow(index - x, 2) + 1);
		}
	}

	public void onHitByBullet(HitByBulletEvent e) {
		if (!enemyWaves.isEmpty()) {
			Point2D.Double hitBulletLocation = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
			EnemyWave hitWave = null;
			for (int x=0; x<enemyWaves.size(); x++) {
				EnemyWave ew = (EnemyWave) enemyWaves.get(x);
				if (Math.abs(ew.distanceTraveled-myLocation.distance(ew.fireLocation))<50 && Math.abs(Util.bulletVelocity(e.getBullet().getPower())-ew.bulletVelocity)<0.001) {
					hitWave = ew;
					break;
				}
			}

			if (hitWave != null) {
				logHit(hitWave, hitBulletLocation);
				enemyWaves.remove(enemyWaves.lastIndexOf(hitWave));
			}
		}
	}

	public Point2D.Double predictPosition(EnemyWave surfWave, int direction) {
		Point2D.Double predictedPosition = (Point2D.Double) myLocation.clone();
		double predictedVelocity = mr.getVelocity();
		double predictedHeading = mr.getHeadingRadians();
		double maxTurning, moveAngle, moveDir;

		int counter = 0;
		boolean intercepted = false;

		do {
			moveAngle = wallSmoothing(predictedPosition, Util.absoluteBearing(surfWave.fireLocation, predictedPosition) + (direction*(Math.PI/2)), direction)-predictedHeading;
			moveDir = 1;

			if (Math.cos(moveAngle)<0) {
				moveAngle += Math.PI;
				moveDir = -1;
			}

			moveAngle = Utils.normalRelativeAngle(moveAngle);

			maxTurning = Math.PI/720d*(40d-3d*Math.abs(predictedVelocity));
			predictedHeading = Utils.normalRelativeAngle(predictedHeading+Util.limit(-maxTurning, moveAngle, maxTurning));

			predictedVelocity += (predictedVelocity*moveDir<0 ? 2*moveDir : moveDir);
			predictedVelocity = Util.limit(-8, predictedVelocity, 8);

			predictedPosition = Util.project(predictedPosition, predictedHeading, predictedVelocity);

			counter++;

			if (predictedPosition.distance(surfWave.fireLocation) < surfWave.distanceTraveled + (counter*surfWave.bulletVelocity) + surfWave.bulletVelocity) {
				intercepted = true;
			}
		} while (!intercepted && counter<500);

		return predictedPosition;
	}

	public double checkDanger(EnemyWave surfWave, int direction) {
		int index = getFactorIndex(surfWave, predictPosition(surfWave, direction));
		return surf_stats[index];
	}

	public void doSurfing() {
		EnemyWave surfWave = getClosestSurfableWave();
		
		if (surfWave==null) {
			return;
		}
		
		double dangerLeft = checkDanger(surfWave, -1);
		double dangerRight = checkDanger(surfWave, 1);

		double goAngle = Util.absoluteBearing(surfWave.fireLocation, myLocation);

		if (dangerLeft < dangerRight) {
			goAngle = wallSmoothing(myLocation, goAngle - (Math.PI/2), -1);
		} else {
			goAngle = wallSmoothing(myLocation, goAngle + (Math.PI/2), 1);
		}

		Util.setBackAsFront(mr, goAngle);
	}

	public double wallSmoothing(Point2D.Double botLocation, double startAngle, int orientation) {
		
		double angle = startAngle;
		double testX = botLocation.x + (Math.sin(angle)*WALL_STICK);
		double testY = botLocation.y + (Math.cos(angle)*WALL_STICK);
		double wallDistanceX = Math.min(botLocation.x-18, mr.getBattleFieldWidth()-botLocation.x-18);
		double wallDistanceY = Math.min(botLocation.y-18, mr.getBattleFieldHeight()-botLocation.y-18);
		double testDistanceX = Math.min(testX-18, mr.getBattleFieldWidth()-testX-18);
		double testDistanceY = Math.min(testY-18, mr.getBattleFieldHeight()-testY-18);
		
		double adjacent = 0;		
	    int g = 0; // because I'm paranoid about potential infinite loops
	 
	    while (!battlefieldRect.contains(testX, testY) && g++ < 25) {
	        if (testDistanceY < 0 && testDistanceY < testDistanceX) {
	            // wall smooth North or South wall
	            angle = ((int)((angle + (Math.PI/2)) / Math.PI)) * Math.PI;
	            adjacent = Math.abs(wallDistanceY);
	        } else if (testDistanceX < 0 && testDistanceX <= testDistanceY) {
	            // wall smooth East or West wall
	            angle = (((int)(angle / Math.PI)) * Math.PI) + (Math.PI/2);
	            adjacent = Math.abs(wallDistanceX);
	        }
	 
	        // use your own equivalent of (1 / POSITIVE_INFINITY) instead of 0.005
	        // if you want to stay closer to the wall ;)
	        angle += orientation*
	            (Math.abs(Math.acos(adjacent/WALL_STICK)) + 0.005);
	 
	        testX = botLocation.x + (Math.sin(angle)*WALL_STICK);
	        testY = botLocation.y + (Math.cos(angle)*WALL_STICK);
	        testDistanceX = Math.min(testX - 18, mr.getBattleFieldWidth() - testX - 18);
	        testDistanceY = Math.min(testY - 18, mr.getBattleFieldHeight() - testY - 18);
	    }
	 
	    return angle; // you may want to normalize this
		
	}
	

	public void onPaint(Graphics2D g) {

	}

}
