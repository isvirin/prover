import Foundation
import Geth
import KeychainSwift

class EthereumService: EthereumServiceProtocol {

  // MARK: - Singleton
  static let shared = EthereumService()
  private init() {
    print("password: \(mainPassword)")
  }
  
  // MARK: - Filemanager properties
  var documentURL: URL {
    let url = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
    return url
  }
  var keystoreURL: URL {
    let appURL = FileManager.default.urls(for: .applicationSupportDirectory,
                                          in: .allDomainsMask).first!
    let url = appURL.appendingPathComponent("keystore")
    return url
  }
  
  // MARK: - Ethereum computed properties
  private lazy var keystore: GethKeyStore = {
    guard let keystore = GethNewKeyStore(keystoreURL.path, GethLightScryptN, GethLightScryptP) else {
      fatalError("Can't create keystore")
    }
    return keystore
  }()
  
  private lazy var mainPassword: String = {
    let keyString = "ethereum password"
    let keychain = KeychainSwift()
    if let password = keychain.get(keyString) {
      return password
    } else {
      let newPassword = UUID().uuidString
      keychain.set(newPassword, forKey: keyString)
      return newPassword
    }
  }()
  
  private var account: GethAccount {
    
    // swiftlint:disable force_try
    if keystore.getAccounts().size() == 0 {
      return try! keystore.newAccount(mainPassword)
    }
    
    if keystore.getAccounts().size() == 1 {
      return try! keystore.getAccounts().get(0)
    }
    // swiftlint:enable force_try
    
    fatalError("More than one accounts")
  }
  
  var hexAddress: String {
    guard let hex = account.getAddress().getHex() else {
      fatalError("Can't get hex from account address")
    }
    return hex
  }
  
  // MARK: - Methods
  func exportWallet(password: String) -> Data? {
    do {
      let key = try keystore.exportKey(account, passphrase: mainPassword, newPassphrase: password)
      let filename = documentURL.appendingPathComponent("wallet \(hexAddress)")
      try key.write(to: filename)
      return key
    } catch {
      print(error.localizedDescription)
      return nil
    }
  }
  
  func successImportWallet(_ data: Data, password: String) -> Bool {
    
    guard let oldKey = try? keystore.exportKey(account, passphrase: mainPassword, newPassphrase: mainPassword) else {
      fatalError("Can't backup old account")
    }
    
    guard (try? keystore.delete(account, passphrase: mainPassword)) != nil else {
      fatalError("Can't delete old account")
    }
    
    if (try? keystore.importKey(data, passphrase: password, newPassphrase: mainPassword)) != nil {
      print("Success import")
      return true
    } else {
      print("Fail to import")
      // swiftlint:disable force_try
      _ = try! keystore.importKey(oldKey, passphrase: mainPassword, newPassphrase: mainPassword)
      // swiftlint:enable force_try
      return false
    }
  }
  
  func getTransactionHex(from text: String, nonce: Hexadecimal,
                         contractAddress: Hexadecimal, gasPrice: Hexadecimal) -> Hexadecimal {
    
    var contractAddressError: NSError?
    let contractAddress = GethNewAddressFromHex(contractAddress.withPrefix, &contractAddressError)
    guard contractAddressError == nil else {
      fatalError("Can't create contract address from hex")
    }
    
    let amount = GethNewBigInt(Int64(0))
    
    let gasLimit = Int64(1000000)
    
    let data = getData(from: text)
    
    let transaction = GethNewTransaction(nonce.toInt64!,
                                         contractAddress,
                                         amount,
                                         gasLimit,
                                         GethBigInt(gasPrice.toInt64!), data)
    
    let signer = account
    
    let chain = GethNewBigInt(3)

    guard let signed = try? keystore
      .signTxPassphrase(signer, passphrase: mainPassword, tx: transaction, chainID: chain) else {
        fatalError("Can't sign transaction")
    }
    
    guard let signedTxData = try? signed.encodeRLP() else {
      fatalError("Can't encode RLP from transaction")
    }
    
    return Hexadecimal(signedTxData.hexDescription)!
  }
  
  // MARK: - Private methods
  private func getData(from text: String) -> Data {
    
    // hardcode of method GENERATE_QRCODE_METHOD
    let method = Hexadecimal("0x708b34fe")!.toBytes
    
    // Преобразуем входящее сообщение в массив байт
    let stringBytes = [UInt8](text.utf8)
    
    // получаем param1Head
    let param1Head = Hexadecimal(32).bigEndian(trailing: 32)

    // получаем param1StrLength на основе длины сообщения
    let param1StrLength = Hexadecimal(stringBytes.count).bigEndian(trailing: 32)
    
    // create param1Value
    let length = stringBytes.count
    let param1ValueLength = (length / 32 + (length % 32 == 0 ? 0 : 1)) * 32
    let param1Value = stringBytes + Array(repeatElement(UInt8(0), count: param1ValueLength - length))
    
    let array = method + param1Head + param1StrLength + param1Value
    
    return Data(array)
  }
}
