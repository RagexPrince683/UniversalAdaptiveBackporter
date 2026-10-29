package ganymedes01.etfuturum.client;
/** Native legacy compass frame convention, shared without client/game dependencies. */
public final class CompassRules {
 private CompassRules(){}
 public static double rotation(double x,double z,double yaw,double targetX,double targetZ){double angle=.25-yaw/360+Math.atan2(targetZ-z,targetX-x)/(Math.PI*2);return angle-Math.floor(angle);}
 public static int frame(double x,double z,double yaw,double targetX,double targetZ){return (int)(rotation(x,z,yaw,targetX,targetZ)*32)&31;}
 public static double spin(long ticks,int id){return Math.floorMod(ticks+id*17L,64L)/64D;}
}
