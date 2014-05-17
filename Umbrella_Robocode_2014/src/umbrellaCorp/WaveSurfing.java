package umbrellaCorp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import robocode.AdvancedRobot;
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
	
	MadRobot mr;
	public static int STATS_SIZE = 47;
	public static double wave_stats[] = new double[STATS_SIZE];
	public Point2D.Double myLocation;
	public Point2D.Double enemyLocation;
	
	public ArrayList<EnemyWave> enemyWaves;
	public ArrayList<Integer> myDirectionsHistory;
	public ArrayList<Double> absBearingsHistory;
	final int historySize = 7;
	final int stepIndexAgo = 2;
	
	public static double opponentEnergy = 100.0;
	public static Rectangle2D.Double battlefieldRect;
	public static double WALL_STICK = 140;
	
	int maxTickToLook = 500;
	
	public WaveSurfing(MadRobot mr) {
		enemyWaves = new ArrayList<EnemyWave>();
		myDirectionsHistory = new ArrayList<Integer>();
		absBearingsHistory = new ArrayList<Double>();
		this.mr=mr;
	}
	
	public void init() {
		battlefieldRect = new java.awt.geom.Rectangle2D.Double(18, 18, mr.getBattleFieldWidth()-36, mr.getBattleFieldHeight()-36);
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		// Posizione robot
		myLocation = new Point2D.Double(mr.getX(), mr.getY());
		// Velocità del robot perpendicolare al robot di riferimento
		double lateralVelocity = mr.getVelocity()*Math.sin(e.getBearingRadians());
		// Absolute bearing tra MyRobot e EnemyRobot
		double absBearing = e.getBearingRadians()+mr.getHeadingRadians();
		// Radar nella direzione del nemico
		mr.setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing-mr.getRadarHeadingRadians()*2));
		// Add in the history direction and bearing
		myDirectionsHistory.add(0, new Integer((lateralVelocity >= 0) ? 1 : -1));
		absBearingsHistory.add(0, new Double(absBearing+Math.PI));
		//Remove if the histories is > of history size
		if(myDirectionsHistory.size()>historySize)
			myDirectionsHistory.remove(myDirectionsHistory.size()-1);
		if(absBearingsHistory.size()>historySize)
			absBearingsHistory.remove(absBearingsHistory.size()-1);
		
		double bulletPower = opponentEnergy - e.getEnergy();
		if(bulletPower<3.01 && bulletPower>0.09 && myDirectionsHistory.size()>stepIndexAgo) {
			EnemyWave ew = new EnemyWave();
			ew.fireTime = mr.getTime()-1;
			ew.bulletVelocity = Util.bulletVelocity(bulletPower);
			ew.distanceTraveled = Util.bulletVelocity(bulletPower);
			ew.direction = ((Integer) myDirectionsHistory.get(stepIndexAgo)).intValue();
			ew.directAngle = ((Double) absBearingsHistory.get(stepIndexAgo)).doubleValue();
			ew.fireLocation = (Point2D.Double) enemyLocation.clone();
			
			enemyWaves.add(ew);
		}
		
		opponentEnergy = e.getEnergy();
		enemyLocation = Util.project(myLocation, absBearing, e.getDistance());
		
		updateWaves();
		doSurfing();
		
	}
	
	// Update the enemyWaves
	public void updateWaves() {
		for (int x = 0; x < enemyWaves.size(); x++) {
			EnemyWave ew = (EnemyWave) enemyWaves.get(x);

			ew.distanceTraveled = (mr.getTime() - ew.fireTime) * ew.bulletVelocity;

			if (ew.distanceTraveled > myLocation.distance(ew.fireLocation) + 50) {
				enemyWaves.remove(x);
				x--;
			}
		}
	}

	public EnemyWave getClosestSurfableWave() {
		double closestDistance = Integer.MAX_VALUE;
		EnemyWave surfWave = null;

		for (int x = 0; x < enemyWaves.size(); x++) {
			EnemyWave ew = (EnemyWave) enemyWaves.get(x);
			double distance = myLocation.distance(ew.fireLocation) - ew.distanceTraveled;

			// We skip the wave that can't avoid (distance <= velocity)
			if (distance > ew.bulletVelocity && distance < closestDistance) {
				surfWave = ew;
				closestDistance = distance;
			}
		}

		return surfWave;
	}


	public static int getFactorIndex(EnemyWave ew, Point2D.Double targetLocation) {
		double offsetAngle = (Util.absoluteBearing(ew.fireLocation, targetLocation) - ew.directAngle);
		double factor = Utils.normalRelativeAngle(offsetAngle) / Util.maxEscapeAngle(ew.bulletVelocity) * ew.direction;
		return (int) Math.round(Util.limit(0, (factor * ((STATS_SIZE - 1) / 2)) + ((STATS_SIZE - 1) / 2), STATS_SIZE - 1));
	}

	public void logHit(EnemyWave ew, Point2D.Double targetLocation) {
		int index = getFactorIndex(ew, targetLocation);

		for (int x = 0; x < STATS_SIZE; x++) {
			wave_stats[x] += 1.0 / (Math.pow(index - x, 2) + 1);
		}
	}

	public void onHitByBullet(HitByBulletEvent e) {
		if (!enemyWaves.isEmpty()) {
			Point2D.Double hitBulletLocation = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
			EnemyWave hitWave = null;
			for (int x = 0; x < enemyWaves.size(); x++) {
				EnemyWave ew = (EnemyWave) enemyWaves.get(x);
				if (Math.abs(ew.distanceTraveled - myLocation.distance(ew.fireLocation)) < 50 && Math.abs(Util.bulletVelocity(e.getBullet().getPower()) - ew.bulletVelocity) < 0.001) {
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
			moveAngle = wallSmoothing(predictedPosition, Util.absoluteBearing(surfWave.fireLocation, predictedPosition) + (direction * (Math.PI / 2)), direction) - predictedHeading;
			moveDir = 1;

			if (Math.cos(moveAngle) < 0) {
				moveAngle += Math.PI;
				moveDir = -1;
			}

			moveAngle = Utils.normalRelativeAngle(moveAngle);

			maxTurning = Math.PI / 720d * (40d - 3d * Math.abs(predictedVelocity));
			predictedHeading = Utils.normalRelativeAngle(predictedHeading + Util.limit(-maxTurning, moveAngle, maxTurning));

			predictedVelocity += (predictedVelocity * moveDir < 0 ? 2 * moveDir : moveDir);
			predictedVelocity = Util.limit(-8, predictedVelocity, 8);

			predictedPosition = Util.project(predictedPosition, predictedHeading, predictedVelocity);

			counter++;

			if (predictedPosition.distance(surfWave.fireLocation) < surfWave.distanceTraveled + (counter * surfWave.bulletVelocity) + surfWave.bulletVelocity) {
				intercepted = true;
			}
		} while (!intercepted && counter < maxTickToLook);

		return predictedPosition;
	}

	public double checkDanger(EnemyWave surfWave, int direction) {
		int index = getFactorIndex(surfWave, predictPosition(surfWave, direction));

		return wave_stats[index];
	}

	public void doSurfing() {
		EnemyWave surfWave = getClosestSurfableWave();

		if (surfWave == null) {
			return;
		}

		double dangerLeft = checkDanger(surfWave, -1);
		double dangerRight = checkDanger(surfWave, 1);

		double goAngle = Util.absoluteBearing(surfWave.fireLocation, myLocation);

		if (dangerLeft < dangerRight) {
			goAngle = wallSmoothing(myLocation, goAngle - (Math.PI / 4), -1);
		} else {
			goAngle = wallSmoothing(myLocation, goAngle + (Math.PI / 4), 1);
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
		EnemyWave wave = null;

		try {
			wave = getClosestSurfableWave();
		} catch (Exception e) {
		
		};

		if (wave == null)
			return;
		int bullet_x = (int) Util.project(wave.fireLocation, wave.directAngle, wave.distanceTraveled).x;
		int bullet_y = (int) Util.project(wave.fireLocation, wave.directAngle, wave.distanceTraveled).y;

		g.drawLine((int) wave.fireLocation.x, (int) wave.fireLocation.y, bullet_x, bullet_y);

		int ray = (int) wave.fireLocation.distance(bullet_x, bullet_y) * 2;
		g.drawOval((int) wave.fireLocation.x - (ray / 2), (int) wave.fireLocation.y - (ray / 2), ray, ray);

		double max = wave_stats[0];
		for (int i = 1; i < STATS_SIZE; i++) {
			if (max < wave_stats[i])
				max = wave_stats[i];
		}

		for (int i = -STATS_SIZE / 2; i < STATS_SIZE / 2; i++) {

			int bx = (int) Util.project(new Point2D.Double(bullet_x, bullet_y), wave.directAngle + Math.PI / 2, i * 5 ).x;
			int by = (int) Util.project(new Point2D.Double(bullet_x, bullet_y), wave.directAngle + Math.PI / 2, i * 5 ).y;

			g.translate(bx, by);
			g.rotate( -wave.directAngle);
			g.setColor(new Color((int) (wave_stats[i + STATS_SIZE / 2] / max * 255), 50, 100));
			g.fillRect(0, 0, 3, 20);
			g.rotate( +wave.directAngle  );
			g.translate(-bx, -by);
		}


	}

}
