package umbrellaCorp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import robocode.ScannedRobotEvent;
import robocode.util.Utils;

class WaveBullet {
		
	public double startX, startY, startBearing, power;
	private long fireTime;
	public int direction;
	private int[] returnSegment;	
	public double enemyDistance;
	public double gf;
	
	public WaveBullet(double x, double y, double bearing, double power, int direction, long time, int[] segment) {
		this.startX = x;
		this.startY = y;
		this.startBearing = bearing;
		this.power = power;
		this.direction = direction;
		this.fireTime = time;
		this.returnSegment = segment;
	}
	
	public boolean checkHit(double enemyX, double enemyY, long currentTime) {
		if (Point2D.distance(startX, startY, enemyX, enemyY) <= (currentTime - fireTime) * Util.bulletVelocity(power)) {
			double desiredDirection = Util.absoluteBearing(new Point2D.Double(startX,startY), new Point2D.Double(enemyX,enemyY));
			double angleOffset = Utils.normalRelativeAngle(desiredDirection - startBearing);
			double guessFactor = Util.limit(-1, angleOffset / Util.maxEscapeAngle(Util.bulletVelocity(power)), 1) * direction;
			int index = (int) Math.round((returnSegment.length-1)/2 * (guessFactor+1));
			returnSegment[index]++;
			return true;
		}
		return false;
	}
	
}
	
/*
 * Guess fra -1 e 1 per capire dove sta andando il robot.
 * 	1 avanti fino al punto pi� lontano che pu� raggiungere andando a max velocit�, 
 * -1 dietro,
 *  0 sta fermo 
 *  
 *  
 */
public class GuessFactorTargeting {

	private List<WaveBullet> waves = new ArrayList<WaveBullet>();
	private MadRobot mr;
	public static final int STATS_SIZE=31;
	private int[] stats = new int[STATS_SIZE];
	private int direction = 1;
	
	private Point2D.Double myLocation = new Point2D.Double();
	private Point2D.Double enemyLocation = new Point2D.Double();
	
	public GuessFactorTargeting(MadRobot mr) {
		this.mr = mr;
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {

		double absBearing = mr.getHeadingRadians() + e.getBearingRadians();

		myLocation.setLocation(mr.getX(), mr.getY());
		enemyLocation.setLocation(Util.project(myLocation, absBearing, e.getDistance()));		
		double ex = enemyLocation.x;
		double ey = enemyLocation.y;
		
		for(int i=0; i<waves.size(); i++) {
			WaveBullet currentWave = (WaveBullet) waves.get(i);
			if (currentWave.checkHit(ex, ey, mr.getTime())) {
				waves.remove(currentWave);
				i--;
			}
		}

		double power = Math.min(3, Math.max(.1, mr.optimalPower(e.getDistance())));

		if (e.getVelocity()!=0) {
			if(Math.sin(e.getHeadingRadians()-absBearing)*e.getVelocity()<0)
				direction=-1;
			else
				direction=1;
		}

		int[] currentStats = stats;
		
		WaveBullet newWave = new WaveBullet(mr.getX(), mr.getY(), absBearing, power, direction, mr.getTime(), currentStats);
		newWave.enemyDistance = e.getDistance();
		
		int bestIndex=(STATS_SIZE-1)/2;
		for(int i=0; i<STATS_SIZE; i++)
			if(currentStats[bestIndex]<currentStats[i])
				bestIndex=i;
		double guessFactor = (double) (bestIndex-(STATS_SIZE-1)/2)/((STATS_SIZE-1)/2);
		newWave.gf = guessFactor;
		double angleOffset = direction*guessFactor*Util.maxEscapeAngle(Util.bulletVelocity(power));
		double gunAdjust = Utils.normalRelativeAngle(absBearing-mr.getGunHeadingRadians()+angleOffset);
		mr.setTurnGunRightRadians(gunAdjust);
		
//		if(mr.setFireBullet(power)!=null)
//			waves.add(newWave);
		
		if(mr.getGunHeat()==0 && gunAdjust<Math.atan2(9, e.getDistance()) && mr.setFireBullet(power)!=null) {
			waves.add(newWave);
		}
				
	}

	public void onPaint(Graphics2D g) {
		WaveBullet wave = null;
		try {
			wave = waves.get(waves.size()-1);
		} catch (Exception e) {
			return;
		}
		g.setColor(Color.CYAN);
		int enemyX = (int) Util.project(new Point2D.Double(wave.startX,
				wave.startY), wave.startBearing, wave.enemyDistance).x;
		int enemyY = (int) Util.project(new Point2D.Double(wave.startX,
				wave.startY), wave.startBearing, wave.enemyDistance).y;

		int enemyMEAX = (int) Util.project(new Point2D.Double(wave.startX,
				wave.startY), wave.startBearing + Util.maxEscapeAngle(wave.power)
				* wave.direction, wave.enemyDistance).x;
		int enemyMEAY = (int) Util.project(new Point2D.Double(wave.startX,
				wave.startY), wave.startBearing + Util.maxEscapeAngle(wave.power)
				* wave.direction, wave.enemyDistance).y;

		g.drawLine(enemyX, enemyY, enemyMEAX, enemyMEAY);

		int bulletX = (int) Util.project(new Point2D.Double(wave.startX,
				wave.startY), wave.startBearing + Util.maxEscapeAngle(Util.bulletVelocity(wave.power))
				* wave.direction * wave.gf, wave.enemyDistance).x;
		int bulletY = (int) Util.project(new Point2D.Double(wave.startX,
				wave.startY), wave.startBearing + Util.maxEscapeAngle(Util.bulletVelocity(wave.power))
				* wave.direction * wave.gf, wave.enemyDistance).y;
		g.fillRect(bulletX, bulletY, 10, 10);
	}
		
}