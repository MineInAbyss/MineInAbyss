package com.derongan.minecraft.mineinabyss.spoofing;

//TODO fix for 1.13
public class SpoofedEntityLiving {
//    private EntityLiving entity;
//    private Location location;
//    private int id;
//
//    public SpoofedEntityLiving(EntityLiving entity, int id, Location location) {
//        this.entity = entity;
//        this.location = location;
//        this.id = id;
//
//        entity.setLocation(location.getX(), location.getY(), location.getZ(), 0, 0);
//    }
//
//    public void moveTo(PlayerConnection conn, Location to) {
//        long dx = (long) (to.getX() * 32 - location.getX() * 32) * 128;
//        long dy = (long) (to.getY() * 32 - location.getY() * 32) * 128;
//        long dz = (long) (to.getZ() * 32 - location.getZ() * 32) * 128;
//
//        location.setX(to.getX());
//        location.setY(to.getY());
//        location.setZ(to.getZ());
//
//        conn.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMove(entity.getId(), dx, dy, dz,false));
//    }
//
//    public void spawn(PlayerConnection con, Runnable additionalSetup) {
//        con.sendPacket(new PacketPlayOutSpawnEntity(entity, id));
//        con.sendPacket(new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), false));
//        additionalSetup.run();
//    }
//
//    public void destroy(PlayerConnection con) {
//        con.sendPacket(new PacketPlayOutEntityDestroy(entity.getId()));
//    }
//
//    public void lookAt(PlayerConnection con, Location to){
//        Vector direction = to.toVector().subtract(location.toVector()).normalize();
//
//        double x = direction.getX();
//        double y = direction.getY();
//        double z = direction.getZ();
//
//        byte yaw = (byte)(((-toDegree(Math.atan2(x, z))) / 360) * 256);
//        byte pitch = 0;//(byte)(((90 - toDegree(Math.acos(y))) / 360) * 256);
//        con.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(entity.getId(), yaw, pitch, false));
//    }
//
//    public EntityLiving getEntity() {
//        return entity;
//    }
//
//    private float toDegree(double angle) {
//        return (float) Math.toDegrees(angle);
//    }
}
