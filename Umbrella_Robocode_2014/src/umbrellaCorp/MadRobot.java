package umbrellaCorp;

import java.awt.Color;
import java.awt.Graphics2D;

import robocode.*;
import robocode.util.Utils;

public class MadRobot extends AdvancedRobot {
	
	private WaveSurfing ws;
	private GuessFactorTargeting gft;
	private MinimumRiskMovement mrm;
	
	public static double battleFieldWidth;
	public static double battleFieldHeight;
	
	private final int HIT_MAX = 3;
	private int currentHit = 0;
	
	public static int ENEMIES;
		
	public void run() {
		init(); 
		setAdjustGunForRobotTurn(true); //Sets the gun to turn independent from the robot's turn. 
		setAdjustRadarForGunTurn(true); //Sets the radar to turn independent from the gun's turn. 
		ENEMIES = getOthers();
		do {
			if(getOthers()>1) /*N VS N*/ {
				mrm.run();
			}
			else /*1 VS 1*/ {
				ws.run();
			}
			execute(); //va chiamata per forza da un AdvancedRobot
		} while(true);
	}
	
	

	/*
	 * EVENT
	 */
	
	@Override
	public void onCustomEvent(CustomEvent e) {
		
	}
	
	/*
	 * ON BULLET HIT
	 * is called when a bullet your robot fires, reaches a target.
	 */
	@Override
	public void onBulletHit(BulletHitEvent e) {
		if(getOthers()==1) /*1 VS 1*/
			currentHit = 0;
		else /*N VS N*/ {
			mrm.onBulletHit(e);
		}
	}
	
	/*
	 * ON BULLED MISSED
	 * is called when one of your bullets misses (hits a wall).
	 */
	@Override
	public void onBulletMissed(BulletMissedEvent e) {
		if(getOthers()==1)
			currentHit++;
	}
	
	/*
	 * ON HIT BY BULLET
	 * is called when your robot is hit by bullet.
	 */
	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		runAway();
		if(getOthers()==1) /*1 VS 1*/
			ws.onHitByBullet(e);
		else /*N VS N*/ {
			mrm.onHitByBullet(e);
		}
	}
	
	/*
	 * ON SCANNED ROBOT
	 * is called when your robot scans another
	 */
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		if(getOthers()>1) /*N VS N*/ {
			mrm.onScannedRobot(e);
		}
		else /*1 VS 1*/ {
			ws.onScannedRobot(e);
			gft.onScannedRobot(e);
			if(currentHit>=HIT_MAX) {
				goToByAngle(e.getBearingRadians(), e.getDistance());
				currentHit = 0;
			}
		}
	}

	/*
	 * ON HIT ROBOT
	 * is called when your robot crash another
	 */
	@Override
	public void onHitRobot(HitRobotEvent e) {
		
	}
	
	/*
	 * ON HIT WALL
	 * is called when your robot crash with a wall
	 */
	@Override
	public void onHitWall(HitWallEvent e) {
//		setBodyColor(Color.BLUE);	//just to test
	}
	
	/*
	 * ON ROBOT DEATH
	 */
	@Override
	public void onRobotDeath(RobotDeathEvent e) {
		if(getOthers()>1) {
			mrm.onRobotDeath(e);
		}
	}
	
	@Override
	public void onWin(WinEvent e) {

	}
	
	/*
	 * END_EVENT
	 */
	
	@Override
	public void onPaint(Graphics2D g) {
		if(getOthers()>1) /*N VS N*/ {
			mrm.onPaint(g);
		}
		else /*1 VS 1*/ {
			ws.onPaint(g);
			gft.onPaint(g);			
		}
	}
	
	public void init() {
		setColors(Color.gray,Color.red,Color.white);
		battleFieldWidth = getBattleFieldWidth();
		battleFieldHeight = getBattleFieldHeight();
		ws = new WaveSurfing(this);
		gft = new GuessFactorTargeting(this);
		mrm = new MinimumRiskMovement(this);
		ws.init();
		mrm.init();
	}
	
	public void goToByAngle(double angleToTarget, double distance) {
		double turnAngle = Math.atan(Math.tan(angleToTarget));
		setTurnRightRadians(turnAngle);
		if(angleToTarget==turnAngle) {
			setAhead(distance);
		}
		else {
			setBack(distance);
		}
	}
	
	public void goToByPoint(double x, double y) {
		
		x -= getX();
		y -= getY();
		
		double angleToTarget = Math.atan2(x, y);	//converte x,y in angolo
		double targetAngle = Utils.normalRelativeAngle(angleToTarget-getHeadingRadians()); // tra -PI e PI
		double distance = Math.hypot(x, y);
	/*  double turnAngle = Math.atan(Math.tan(targetAngle));
		setTurnRightRadians(turnAngle);
		if(targetAngle==turnAngle) {
			setAhead(distance);
		}
		else {
			setBack(distance);
		}
			*/
		goToByAngle(targetAngle,distance);
	}
	
		
	public void runAway() {
		
		final double DIMENSION = 100;

		double suggestedBattleFieldWidth = battleFieldWidth-DIMENSION;
		double suggestedBattleFieldHeight = battleFieldHeight-DIMENSION;
		
		double futureX = Math.random()*suggestedBattleFieldWidth;
		double futureY = Math.random()*suggestedBattleFieldHeight;
		
		goToByPoint(futureX, futureY);
	}
	
	public double optimalPower(double distance) {
		final double minPower = 0.1;
		final double maxPower = 3.0;
		final double minDistance = 200;
		double currentEnergy = getEnergy();
		double power;
		if(distance<=minDistance)
			power = maxPower;
		else 
			power = maxPower*minDistance/distance;
		if(currentEnergy<power)
			power=minPower;
		return power;
	}
	
}

