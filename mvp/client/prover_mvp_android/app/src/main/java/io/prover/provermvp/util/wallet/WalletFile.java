package io.prover.provermvp.util.wallet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import io.prover.provermvp.util.UtilFile;

/**
 * Ethereum wallet file.
 */
public class WalletFile {
    public final Crypto crypto;
    public final String id;
    public final int version;

    public WalletFile(Crypto crypto, String id, int version) {
        this.crypto = crypto;
        this.id = id;
        this.version = version;
    }

    public WalletFile(JSONObject jsonObject) throws JSONException {
        version = jsonObject.getInt("version");
        id = jsonObject.getString("id");
        crypto = new Crypto(jsonObject.getJSONObject("crypto"));
    }

    public WalletFile(String source) throws JSONException {
        this(new JSONObject(source.toLowerCase()));
    }

    public WalletFile(File file) throws JSONException {
        this(UtilFile.readFully(file));
    }

    public JSONObject toJson() throws JSONException {
        JSONObject res = new JSONObject();
        res.put("version", version);
        res.put("id", id);
        res.put("crypto", crypto.toJson());
        return res;
    }

    public void writeToFile(File file) throws FileNotFoundException, UnsupportedEncodingException, JSONException {
        PrintStream outputStream = new PrintStream(new FileOutputStream(file), false, "UTF-8");
        outputStream.append(toJson().toString());
        outputStream.flush();
        outputStream.close();
    }

    interface KdfParams {
        JSONObject toJson() throws JSONException;
    }

    public static class Crypto {
        public final String cipher;
        public final String ciphertext;
        public final CipherParams cipherparams;

        public final String kdf;
        public final KdfParams kdfparams;

        public final String mac;

        public Crypto(String cipher, byte[] cipherText, CipherParams cipherparams, String kdf, KdfParams kdfparams, byte[] mac) {
            this.cipher = cipher;
            this.ciphertext = Numeric.toHexStringNoPrefix(cipherText);
            this.cipherparams = cipherparams;
            this.kdf = kdf;
            this.kdfparams = kdfparams;
            this.mac = Numeric.toHexStringNoPrefix(mac);
        }

        public Crypto(JSONObject src) throws JSONException {
            cipher = src.getString("cipher");
            ciphertext = src.getString("ciphertext");

            cipherparams = new CipherParams(src.getJSONObject("cipherparams"));
            kdf = src.getString("kdf");
            if ("scrypt".equalsIgnoreCase(kdf)) {
                kdfparams = new ScryptKdfParams(src.getJSONObject("kdfparams"));
            } else {
                kdfparams = new Aes128CtrKdfParams(src.getJSONObject("kdfparams"));
            }
            mac = src.getString("mac");
        }

        public JSONObject toJson() throws JSONException {
            JSONObject crypto = new JSONObject();
            crypto.put("ciphertext", ciphertext);
            crypto.put("cipherparams", cipherparams.toJson());
            crypto.put("cipher", cipher);
            crypto.put("kdf", kdf);
            crypto.put("kdfparams", kdfparams.toJson());
            crypto.put("mac", mac);
            return crypto;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Crypto)) return false;

            Crypto crypto = (Crypto) o;

            if (!cipher.equals(crypto.cipher)) return false;
            if (!ciphertext.equals(crypto.ciphertext)) return false;
            if (!cipherparams.equals(crypto.cipherparams)) return false;
            if (!kdf.equals(crypto.kdf)) return false;
            if (!kdfparams.equals(crypto.kdfparams)) return false;
            return mac.equals(crypto.mac);
        }

        @Override
        public int hashCode() {
            int result = cipher.hashCode();
            result = 31 * result + ciphertext.hashCode();
            result = 31 * result + cipherparams.hashCode();
            result = 31 * result + kdf.hashCode();
            result = 31 * result + kdfparams.hashCode();
            result = 31 * result + mac.hashCode();
            return result;
        }
    }

    public static class CipherParams {
        public final String iv;

        public CipherParams(JSONObject cipherparams) throws JSONException {
            iv = cipherparams.getString("iv");
        }

        public CipherParams(byte[] iv) {
            this.iv = Numeric.toHexStringNoPrefix(iv);
        }

        public JSONObject toJson() throws JSONException {
            JSONObject res = new JSONObject();
            res.put("iv", iv);
            return res;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CipherParams)) return false;

            CipherParams that = (CipherParams) o;

            return iv.equals(that.iv);
        }

        @Override
        public int hashCode() {
            return iv.hashCode();
        }
    }

    public static class Aes128CtrKdfParams implements KdfParams {
        public final int dklen;
        public final int c;
        public final String prf;
        public final String salt;

        public Aes128CtrKdfParams(JSONObject kdfparams) throws JSONException {
            dklen = kdfparams.getInt("dklen");
            c = kdfparams.getInt("c");
            prf = kdfparams.getString("prf");
            salt = kdfparams.getString("salt");
        }

        public Aes128CtrKdfParams(int dklen, int c, String prf, String salt) {
            this.dklen = dklen;
            this.c = c;
            this.prf = prf;
            this.salt = salt;
        }

        @Override
        public JSONObject toJson() throws JSONException {
            JSONObject res = new JSONObject();
            res.put("dklen", dklen);
            res.put("c", c);
            res.put("prf", prf);
            res.put("salt", salt);
            return res;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Aes128CtrKdfParams)) return false;

            Aes128CtrKdfParams that = (Aes128CtrKdfParams) o;

            if (dklen != that.dklen) return false;
            if (c != that.c) return false;
            if (!prf.equals(that.prf)) return false;
            return salt.equals(that.salt);
        }

        @Override
        public int hashCode() {
            int result = dklen;
            result = 31 * result + c;
            result = 31 * result + prf.hashCode();
            result = 31 * result + salt.hashCode();
            return result;
        }
    }

    public static class ScryptKdfParams implements KdfParams {
        public final int dklen;
        public final int n;
        public final int p;
        public final int r;
        public final String salt;

        public ScryptKdfParams(int dklen, int n, int p, int r, byte[] salt) {
            this.dklen = dklen;
            this.n = n;
            this.p = p;
            this.r = r;
            this.salt = Numeric.toHexStringNoPrefix(salt);
        }

        public ScryptKdfParams(JSONObject kdfparams) throws JSONException {
            dklen = kdfparams.getInt("dklen");
            salt = kdfparams.getString("salt");
            n = kdfparams.getInt("n");
            r = kdfparams.getInt("r");
            p = kdfparams.getInt("p");
        }

        @Override
        public JSONObject toJson() throws JSONException {
            JSONObject res = new JSONObject();
            res.put("dklen", dklen);
            res.put("salt", salt);
            res.put("n", n);
            res.put("r", r);
            res.put("p", p);
            return res;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ScryptKdfParams)) return false;

            ScryptKdfParams that = (ScryptKdfParams) o;

            if (dklen != that.dklen) return false;
            if (n != that.n) return false;
            if (p != that.p) return false;
            if (r != that.r) return false;
            return salt.equals(that.salt);
        }

        @Override
        public int hashCode() {
            int result = dklen;
            result = 31 * result + n;
            result = 31 * result + p;
            result = 31 * result + r;
            result = 31 * result + salt.hashCode();
            return result;
        }
    }
}
