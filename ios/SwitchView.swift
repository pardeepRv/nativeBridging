import UIKit
import MapKit

class SwitchView: UIView, MKMapViewDelegate {
  
  /*@objc var isOn: Bool = false  {
    didSet {
          button.backgroundColor = isOn ? .yellow : .black
          button.setTitle(String(describing: isOn ? "I am ON" : "I am OFF"), for: .normal)
     }
  }*/
  override init(frame: CGRect) {
    super.init(frame: frame)
  //  self.addSubview(button)
   self.addSubview(Apple)
   // addapple()
  }
  required init?(coder aDecoder: NSCoder) {
    fatalError("init has not been implemented")
  }
/* lazy var button: UIButton = {
      let button = UIButton.init(type: UIButton.ButtonType.system)
      button.autoresizingMask = [.flexibleWidth, .flexibleHeight]
      button.titleLabel?.font = UIFont.systemFont(ofSize: 20)
      button.addTarget(
        self,
        action: #selector(toggleSwitchStatus),
        for: .touchUpInside
      )
      return button
    }()
  
  */
  
  var Apple: MKMapView = {
   let mapView = MKMapView()
      mapView.frame = CGRect(x: 0, y: 0, width: 414, height: 896)
      mapView.mapType = MKMapType.standard
   
      mapView.isZoomEnabled = true
     mapView.isScrollEnabled = true
   mapView.isUserInteractionEnabled = true
 //  mapView.backgroundColor = .red
//  MBXMBTilesOverlay *mbtilesOverlay;
   //let obj = MBXMBTilesOverlay()

   var filePath = Bundle.main.url(forResource: "MBTILES_08", withExtension: "mbtiles")
   print("path:",filePath?.absoluteString)
  var mbtilesOverlay = MBXMBTilesOverlay()
  mbtilesOverlay = MBXMBTilesOverlay(mbTilesPath:filePath?.absoluteString)
   print("mbtilesOverlay:",mbtilesOverlay)
  // mbtilesOverlay = [[MBXMBTilesOverlay alloc] initWithMBTilesPath:mbtilesPath];
  // [mapView addOverlay:mbtilesOverlay];
   mapView.addOverlay(mbtilesOverlay as! MKOverlay)
   
      return mapView
  }()
  
 
  /*
  func addapple() {
    let mapView = MKMapView()
       mapView.frame = CGRect(x: 0, y: 0, width: 150, height: 250)
       mapView.mapType = MKMapType.standard
    mapView.delegate = self
       mapView.isZoomEnabled = true
      mapView.isScrollEnabled = true
    mapView.isUserInteractionEnabled = true
  //  mapView.backgroundColor = .red

    let obj = MBXMBTilesOverlay()
    
    var filePath = Bundle.main.url(forResource: "MBTILES_08", withExtension: "mbtiles")
    print("path:",filePath?.absoluteString)
    var mbtilesOverlay: MBXMBTilesOverlay?
    mbtilesOverlay = MBXMBTilesOverlay(mbTilesPath:filePath?.absoluteString)
    print("mbtilesOverlay:",mbtilesOverlay)
  mapView.addOverlay(mbtilesOverlay as! MKOverlay)
    self.addSubview(mapView)
  }
  
*/
  
  
  
  @objc func toggleSwitchStatus() {
   // isOn = !isOn as Bool
  }
}
