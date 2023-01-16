package daemon.dev.field.nypt;

class Aes implements Symmetric{

  byte[][] subBox = new byte[16][16];
  byte[][] invBox = new byte[16][16];

  short GEN_POLY = (short)0b0100011011;

  int size = 0;

  public Aes(){
    subBox = sBox();
    invBox = invBox();
  }

  /* aes can only use 128, 192, 256 bit
  or 16, 24, 32 byte key lengths */
//  public byte[] createKey(int length){
//
//    byte[] key = new byte[length];
//
//    for (int i=0;i<length;i++){
//      key[i] = (byte)rand.nextInt(50);
//    }
//
//    return key;
//  }

  public byte[] encrypt(byte[] key, byte[] open){

    byte[][][] state = createState(open);

    byte[][] roundKeys = createRoundKeys(key);

    byte[][] rK = new byte[4][4];
    for(int r=0;r<4;r++){rK[r] = roundKeys[r];}

    state = addRoundKey(rK,state);

    for(int i=1;i<10;i++){

      state = subBytes(state);
      state = shiftRows(state);
      state = mixColumns(state);

      for(int r=0;r<4;r++){rK[r] = roundKeys[i*4+r];}
      state = addRoundKey(rK,state);
    }

    state = subBytes(state);
    state = shiftRows(state);

    for(int r=0;r<4;r++){rK[r] = roundKeys[40+r];}
    state = addRoundKey(rK,state);

    return createSequence(state);
  }

  public byte[] decrypt(byte[] key, byte[] close){

        byte[][][] state = createState(close);

        byte[][] roundKeys = createRoundKeys(key);

        byte[][] rK = new byte[4][4];
        for(int r=0;r<4;r++){rK[r] = roundKeys[40+r];}

        state = addRoundKey(rK,state);

        for(int i=9;i>0;i--){

          state = invShiftRows(state);
          state = invSubBytes(state);

          for(int r=0;r<4;r++){rK[r] = roundKeys[i*4+r];}
          state = addRoundKey(rK,state);

          state = invMixColumns(state);
        }

        state = invShiftRows(state);
        state = invSubBytes(state);

        for(int r=0;r<4;r++){rK[r] = roundKeys[r];}
        state = addRoundKey(rK,state);

        return createSequence(state);
  }

  private byte[][][] createState(byte[] open){

    size = open.length/16;

    byte[][][] state = new byte[size][4][4];

    for(int i=0;i<size;i++){

      byte[][] s = new byte[4][4];

      for(int t=0;t<16;t++){

        int row = t%4;
        int col = (int)(t/4);
        s[row][col] = open[t+i*16];

      }

      state[i] = s;

    }

    return state;
  }

  private byte[] createSequence(byte[][][] state){

    int len = state.length;
    byte[] sequence = new byte[len*16];

    for(int i=0;i<len;i++){
        for(int t=0;t<16;t++){
          int row = t%4;
          int col = (int)(t/4);
          sequence[t+i*16] = state[i][row][col];
        }
    }

    return sequence;
  }

  private byte[][][] addRoundKey(byte[][] rKey, byte[][][] state){
    for(int i=0;i<state.length;i++){
      for(int c=0;c<4;c++){
        for(int r=0;r<4;r++){
          state[i][r][c] = (byte)(rKey[c][r]^state[i][r][c]);
        }
      }
    }
    return state;
  }

  private byte[][][] mixColumns(byte[][][] state){

    short hex2 = (short)0x02;
    short hex3 = (short)0x03;

    for(int i=0;i<state.length;i++){
      for(int c=0;c<4;c++){
        short c0 = (short)(state[i][0][c]&0xFF);
        short c1 = (short)(state[i][1][c]&0xFF);
        short c2 = (short)(state[i][2][c]&0xFF);
        short c3 = (short)(state[i][3][c]&0xFF);
        state[i][0][c] = (byte)(_ffmult(hex2,c0)^_ffmult(hex3,c1)^c2^c3);
        state[i][1][c] = (byte)(c0^_ffmult(hex2,c1)^_ffmult(hex3,c2)^c3);
        state[i][2][c] = (byte)(c0^c1^_ffmult(hex2,c2)^_ffmult(hex3,c3));
        state[i][3][c] = (byte)(_ffmult(hex3,c0)^c1^c2^_ffmult(hex2,c3));
      }
    }
    return state;
  }

  private byte[][][] shiftRows(byte[][][] state){

    byte[] nrow = new byte[4];

    for(int i=0;i<state.length;i++){
      for(int r=0;r<4;r++){
        for(int c=0;c<4;c++){
          nrow[c] = state[i][r][(c+r)%4];
        }
        for(int t=0;t<4;t++){state[i][r][t] = nrow[t];}
      }
    }

    return state;
  }

  private byte[][][] subBytes(byte[][][] state){
    for(int i=0;i<state.length;i++){
      for(int c=0;c<4;c++){
        for(int r=0;r<4;r++){
          int x = (int)((state[i][r][c]>>4)&0xf);
          int y = (int)(state[i][r][c]&0xf);
          state[i][r][c] = subBox[x][y];
        }
      }
    }
    return state;
  }

  private byte[][][] invMixColumns(byte[][][] state){

    short hex9 = (short)0x09;
    short hexb = (short)0x0b;
    short hexd = (short)0x0d;
    short hexe = (short)0x0e;

    for(int i=0;i<state.length;i++){
      for(int c=0;c<4;c++){
        short c0 = (short)(state[i][0][c]&0xFF);
        short c1 = (short)(state[i][1][c]&0xFF);
        short c2 = (short)(state[i][2][c]&0xFF);
        short c3 = (short)(state[i][3][c]&0xFF);

        state[i][0][c] = (byte)(_ffmult(hexe,c0)^_ffmult(hexb,c1)^
        _ffmult(hexd,c2)^_ffmult(hex9,c3));
        state[i][1][c] = (byte)(_ffmult(hex9,c0)^_ffmult(hexe,c1)^
        _ffmult(hexb,c2)^_ffmult(hexd,c3));
        state[i][2][c] = (byte)(_ffmult(hexd,c0)^_ffmult(hex9,c1)^
        _ffmult(hexe,c2)^_ffmult(hexb,c3));
        state[i][3][c] = (byte)(_ffmult(hexb,c0)^_ffmult(hexd,c1)^
        _ffmult(hex9,c2)^_ffmult(hexe,c3));
      }
    }
    return state;
  }

  private byte[][][] invShiftRows(byte[][][] state){

    byte[] nrow = new byte[4];

    for(int i=0;i<state.length;i++){
      for(int r=0;r<4;r++){
        for(int c=0;c<4;c++){
          nrow[c] = state[i][r][c];
        }
        for(int t=0;t<4;t++){state[i][r][(t+r)%4] = nrow[t];}
      }
    }

    return state;
  }

  private byte[][][] invSubBytes(byte[][][] state){
    for(int i=0;i<state.length;i++){
      for(int c=0;c<4;c++){
        for(int r=0;r<4;r++){
          int x = (state[i][r][c]>>4)&0xf;
          int y = state[i][r][c]&0xf;
          state[i][r][c] = invBox[x][y];
        }
      }
    }
    return state;
  }

  private byte[][] createRoundKeys(byte[] key){

    int n = 4;
    int r = 11;
    byte[][] words = new byte[r*n][4];

    for(int i=0;i<(r*n);i++){

        if (i<n){
          for(int b=0;b<4;b++){words[i][b]=key[i*4+b];}
        }

        else if ((i>=n) && (i%n==0)){
          byte[] subWord = subWord(rotWord(words[i-1]));
          byte[] rcon = {(byte)rcon(i/n),(byte)0x00,(byte)0x00,(byte)0x00};
          for (int b=0;b<4;b++){words[i][b] = (byte)(words[i-n][b]^subWord[b]^rcon[b]);}
        }

        // case i>=n && n>6 && i%n==4:
        // byte[] subWord = subWord(words[i-1]);
        // for (int b=0;b<4;b++){words[i] = words[i-n][b]^subWord[b];}
        // break;

        else{
          for(int b=0;b<4;b++){words[i][b] = (byte)(words[i-n][b]^words[i-1][b]);}
        }

    }

    return words;
  }

  private byte[] rotWord(byte[] word){
    byte[] rotWord = new byte[4];
    for(int i=0;i<4;i++){rotWord[i] = word[(i+1)%4];}
    return rotWord;
  }

  private int rcon(int i){
    int rc = 0;

      if (i==1){
        rc = 1;
      }

      else if ((i>1) && (rcon(i-1)<0x80)){
        rc = 2*rcon(i-1);
      }

      else if ((i>1) && (rcon(i-1)>=0x80)){
        rc = (2*rcon(i-1))^0x11B;
      }

      else{System.out.println("Help error");}

    return rc;

  }

  private byte[] subWord(byte[] word){
    byte[] subWord = new byte[4];

    for(int i=0;i<4;i++){

      int x = (int)((word[i]>>4)&0xf);
      int y = (int)(word[i]&0xf);

      subWord[i] = subBox[x][y];

    }

    return subWord;
  }

  private byte[][] sBox(){

    byte[][] sBox = new byte[16][16];

    byte c = (byte)0x63;

    for (int i=0;i<16;i++){
      for(int ii=0;ii<16;ii++){

        byte x = (byte)((i << 4) | ii);

        if(x == 0 || x == 1){}
        else{ x = invGf8(x);}

        byte entry = 0;

        for(int s=0;s<8;s++){
          entry = (byte)(entry|
          (((c>>s)&0b1)^
          ((x>>s)&0b1)^
          ((x>>((s+4)%8))&0b1)^
          ((x>>((s+5)%8))&0b1)^
          ((x>>((s+6)%8))&0b1)^
          ((x>>((s+7)%8))&0b1))
          <<s);
        }
        sBox[i][ii] = entry;
      }
    }

    return sBox;
  }

  private byte[][] invBox(){

    byte[][] sBox = new byte[16][16];

    byte c = (byte)0x05;

    for (int i=0;i<16;i++){
      for(int ii=0;ii<16;ii++){

        byte x = (byte)((i << 4) | ii);
        byte entry = 0;

        for(int s=0;s<8;s++){
          entry = (byte)(entry|
          (((c>>s)&0b1)^
          ((x>>((s+2)%8))&0b1)^
          ((x>>((s+5)%8))&0b1)^
          ((x>>((s+7)%8))&0b1))
          <<s);
        }

        if(entry == 0 || entry == 1){}
        else{ entry = invGf8(entry);}

        sBox[i][ii] = entry;
      }
    }

    return sBox;
  }

  private byte invGf8(byte b){

    short q = 0; short temp = 0;
    short t = 0; short nextt = (short)0b0001;
    short r = GEN_POLY; short nextr = (short)(b&0xFF);

    while( nextr != 0){

      q = ffdiv(r,nextr);

      temp = nextr;
      nextr = (short)(r^ffmult(q,nextr));
      r = temp;

      temp = nextt;
      nextt = (short)(t^ffmult(q,nextt));
      t = temp;
    }

    return (byte)t;

  }

  private short ffmult(short a, short b){

    short r = 0;
    for (int i=0;i<8;i++){
      if((a&(1<<i)) == 0){}
      else{
        for(int q=0;q<8;q++){
          if((b&(1<<q)) == 0){}
          else{
            r = (short)(r^(1<<(q+i)));
          }
        }
      }
    }
    return r;
  }

  private short ffdiv(short a, short b){

    int alen = getBitLen(a);
    int blen = getBitLen(b);
    int i = alen - blen;

    short r = (short)(1 << i);
    short e = (short)(a^(b << i));

    while(getBitLen(e) >= blen){
      i--;
      if((e&(1<<(blen-1+i))) != 0){
        e = (short)(e^(b<<i));
        r = (short)(r ^ (1 << i));
      }
    }
    return r;
  }

  private short ffmod(short a, short b){

    int alen = getBitLen(a);
    int blen = getBitLen(b);
    int i = alen - blen;

    short r = (short)(1 << i);
    short e = (short)(a^(b << i));

    while(getBitLen(e) >= blen){
      i--;
      if((e&(1<<(blen-1+i))) != 0){
        e = (short)(e^(b<<i));
        r = (short)(r ^ (1 << i));
      }
    }
    return e;
  }

  private short _ffmult(short a, short b){

    short r = 0;
    for (int i=0;i<8;i++){
      if((a&(1<<i)) == 0){}
      else{
        for(int q=0;q<8;q++){
          if((b&(1<<q)) == 0){}
          else{
            r = (short)(r^(1<<(q+i)));
          }
        }
      }
    }
    return ffmod(r,GEN_POLY);
  }

  private int getBitLen(short b){
    int i = 14;
    while((b&(1<<i))==0){i--;if(i==-1){break;}}
    return i+1;
  }

}
