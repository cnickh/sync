package daemon.dev.field.nypt;

public interface Symmetric{

  public byte[] encrypt(byte[] key, byte[] open);

  public byte[] decrypt(byte[] key, byte[] close);

}
