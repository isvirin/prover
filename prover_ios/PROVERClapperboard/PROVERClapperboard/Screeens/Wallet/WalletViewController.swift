//
//  WalletViewController.swift
//  PROVERClapperboard
//
//  Created by Mac Mini on 13/03/2018.
//  Copyright © 2018 Nordavind. All rights reserved.
//

import UIKit

class WalletViewController: UITableViewController {
  
  @IBAction func cancelButtonAction(_ sender: UIBarButtonItem) {
    navigationController?.dismiss(animated: true, completion: nil)
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    guard let navigationBar = navigationController?.navigationBar else { return }
    
    let size = CGSize(width: navigationBar.bounds.width,
                      height: navigationBar.bounds.height + UIApplication.shared.statusBarFrame.height)
    let image = #imageLiteral(resourceName: "background").resizedImage(newSize: size)
    navigationBar.barTintColor = UIColor(patternImage: image)
    
    tableView.rowHeight = UITableViewAutomaticDimension
    tableView.estimatedRowHeight = 44
  }
}

// MARK: - UITableViewDelegate
extension WalletViewController {
  override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
    return UITableViewAutomaticDimension
  }
}
