package umbrellaCorp;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import robocode.ScannedRobotEvent;
import robocode.util.Utils;

class WaveBullet {
		
	private double startX, startY, startBearing, power;
	private long fireTime;
	private int direction;
	private int[] returnSegment;	
	
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
	

public class GuessFactorTargeting {

	private List<WaveBullet> waves = new ArrayList<WaveBullet>();
	private MadRobot mr;
	public static int STATS_SIZE=31;
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
		
		for (int i=0; i<waves.size(); i++) {
			WaveBullet currentWave = (WaveBullet) waves.get(i);
			if (currentWave.checkHit(ex, ey, mr.getTime())) {
				waves.remove(currentWave);
				i--;
			}
		}

		double power = Math.min(3, Math.max(.1, optimalPower(e.getDistance())));

		if (e.getVelocity()!=0) {
			if(Math.sin(e.getHeadingRadians()-absBearing)*e.getVelocity()<0)
				direction=-1;
			else
				direction=1;
		}

		int[] currentStats = stats;
		
		WaveBullet newWave = new WaveBullet(mr.getX(), mr.getY(), absBearing, power, direction, mr.getTime(), currentStats);
		
		int bestIndex=(STATS_SIZE-1)/2;
		for(int i=0; i<STATS_SIZE; i++)
			if(currentStats[bestIndex]<currentStats[i])
				bestIndex=i;
		double guessFactor = (double) (bestIndex-(stats.length-1)/2)/((stats.length-1)/2);
		double angleOffset = direction*guessFactor*Util.maxEscapeAngle(Util.bulletVelocity(power));
		double gunAdjust = Utils.normalRelativeAngle(absBearing-mr.getGunHeadingRadians()+angleOffset);
		mr.setTurnGunRightRadians(gunAdjust);
		
		if(mr.setFireBullet(power)!=null)
			waves.add(newWave);
		
		if(mr.getGunHeat()==0 && gunAdjust<Math.atan2(9, e.getDistance()) && mr.setFireBullet(power)!=null) {
			waves.add(newWave);
		}
				
	}
	
	
	public double optimalPower(double distance) {
		final double minPower = 0.1;
		final double maxPower = 3.0;
		final double minDistance = 150;
		double currentEnergy = mr.getEnergy();
		double power;
		if(distance<=minDistance)
			power = maxPower;
		else 
			power = maxPower*minDistance/distance;
		if(currentEnergy<power)
			power=minPower;
		return power;
	}


	public void onPaint(Graphics2D g) {

	}
		
}