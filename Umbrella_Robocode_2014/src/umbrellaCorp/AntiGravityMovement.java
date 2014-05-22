package umbrellaCorp;

import java.awt.geom.Point2D;

import robocode.ScannedRobotEvent;

public class AntiGravityMovement {

	private MadRobot mr;
//	private Point2D.Double gravitation;
//	private Point2D.Double closest;
//	private Point2D.Double lastFireTime;
	
	private Point2D.Double[] enemyPoints;
	private int count;
		
	public AntiGravityMovement(MadRobot mr) {
		this.mr = mr;
//		gravitation = new Point2D.Double(0,0);
//		closest = new Point2D.Double(Double.POSITIVE_INFINITY, 0);
//		lastFireTime = new Point2D.Double(0,0);
		
		enemyPoints = new Point2D.Double[mr.getOthers()];
		count = 0;
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		double absBearing = e.getBearingRadians()+mr.getHeadingRadians();
		enemyPoints[count] = new Point2D.Double(mr.getX()+e.getDistance()*Math.sin(absBearing), mr.getY()+e.getDistance()*Math.cos(absBearing));
		if(++count>=mr.getOthers()) {
			count = 0;
		}
		
//		 if(((gravitation.x=gravitation.x*0.8-(1/e.getDistance()*Math.sin(e.getBearingRadians()+mr.getHeadingRadians())))+(gravitation.y=gravitation.y*0.8-(1/e.getDistance()*Math.cos(e.getBearingRadians()+mr.getHeadingRadians())))!=0d || true) 
//					&& e.getDistance()<closest.x+180 && (closest.x=e.getDistance())>0 || mr.getTime()-lastFireTime.x>36) {
//				if(((mr.getEnergy()>0.11d || e.getEnergy()==0d)
//							&& mr.setFireBullet(Math.min(900d/e.getDistance(),Math.min(mr.getEnergy()/5d,Math.min(3d,e.getEnergy()/6d))))!=null
//							&& (lastFireTime.x=mr.getTime())>0)
//							&& (closest.x=Double.POSITIVE_INFINITY)>0 || true) {
//				        mr.setTurnGunRight(Double.POSITIVE_INFINITY);
//				}
//		 } 
	}
	
	public void doMove() {
//		mr.setAhead(180*12/Math.abs(lastFireTime.y=Math.toDegrees(robocode.util.Utils.normalRelativeAngle(Math.atan2(gravitation.x+ 1 / (mr.getX()-2d) - 1 / (mr.getBattleFieldWidth() - mr.getX()-2d),gravitation.y+ 1 / (mr.getY()-2d) - 1 / (mr.getBattleFieldHeight() - mr.getY()-2d))-mr.getHeadingRadians()))));
//		mr.turnRight(Math.abs(lastFireTime.y)<10d ? 
//			lastFireTime.y
//			: (lastFireTime.y >0d ? 10d : -10d));
		
	}
		
}
