package umbrellaCorp;

import java.awt.Color;
import java.awt.Graphics2D;

import robocode.*;
import robocode.util.Utils;

public class MadRobot extends AdvancedRobot {
	
	private WaveSurfing ws = new WaveSurfing(this);
	private GuessFactorTargeting gft = new GuessFactorTargeting(this);
	private MinimumRiskMovement mrm = new MinimumRiskMovement(this);
	
	private final int HIT_MAX = 3;
	private int currentHit = 0;
	
	public static int ENEMIES;
	
	private boolean isBlind;
	
	public void run() {
		init(); 
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		ws.init();
		mrm.init();
		ENEMIES = getOthers();
		do {
//			if(isBlind) {
//				turnRadarRight(Double.POSITIVE_INFINITY);
//				searchRobot();
//			} 
			if(getOthers()>1) /*N VS N*/ {
				mrm.run();
			}
			else /*1 VS 1*/ {
				turnRadarRight(Double.POSITIVE_INFINITY);
			}
			execute();
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
		isBlind = false;
		if(getOthers()>1) /*N VS N*/ {
			mrm.onScannedRobot(e);
		}
		else /*1 VS 1*/ {
			ws.onScannedRobot(e);
			gft.onScannedRobot(e);
			if(currentHit>=HIT_MAX) {
				goToAngle(e.getBearingRadians(), e.getDistance());
				currentHit = 0;
			}
		}
		isBlind = true;
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
//		setBodyColor(Color.BLUE);
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
		isBlind = true;
	}
	
	public void goTo(double x, double y) {
		x -= getX();
		y -= getY();
		
		double angleToTarget = Math.atan2(x, y);
		double targetAngle = Utils.normalRelativeAngle(angleToTarget-getHeadingRadians());
		double distance = Math.hypot(x, y);
		double turnAngle = Math.atan(Math.tan(targetAngle));
		setTurnRightRadians(turnAngle);
		if(targetAngle==turnAngle) {
			setAhead(distance);
		}
		else {
			setBack(distance);
		}
	}
	
	public void goToAngle(double angleToTarget, double distance) {
		double turnAngle = Math.atan(Math.tan(angleToTarget));
		setTurnRightRadians(turnAngle);
		if(angleToTarget==turnAngle) {
			setAhead(distance);
		}
		else {
			setBack(distance);
		}
	}
	
	public void searchRobot() {
		double battleFieldWidth = getBattleFieldWidth()-36;
		double battleFieldHeight = getBattleFieldHeight()-36;
		double myX = getX();
		double myY = getY();
		if(myX>battleFieldWidth/2 && myY>battleFieldHeight/2)
			goTo(36, 36); /*Angolo superiore sx*/
		else if(myX>battleFieldWidth/2 && myY<=battleFieldHeight/2)
			goTo(36, battleFieldHeight); /*Angolo inferiore sx*/
		else if(myX<=battleFieldWidth/2 && myY>battleFieldHeight/2)
			goTo(getBattleFieldWidth(),36); /*Angolo superiore dx*/
		else if(myX<=battleFieldWidth/2 && myY<=battleFieldHeight/2)
			goTo(getBattleFieldWidth(),getBattleFieldHeight());
		else /*Condizione di sicurezza*/ {
			goTo(getBattleFieldWidth(),getBattleFieldHeight());
		}
	}
	
}

