package finalproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectV1 extends Project {

	/** 
	 * As a general purchase procedure:
	 *	1) A shopper logs in
	 *	2) Shopper adds items to the cart
	 *	3) If available, items get added accordingly
	 *	4) Checkout (method in API, or button in GUI) generates a new order (and automatically computes 
	 *     shipping and generates a new invoice).
     *
	 *	Now - a word on inventory maintenance - explained via an example.
     *
	 *	Say shopper s adds product p in his cart; the desired quantity comes from warehouse A and warehouse B because none 
	 *  of them has enough.
	 *	Please observe at this stage the shopper has not checked out yet; the shopper is still logged in to the system. 
	 *	To make it more practical, say warehouse A has 10 pieces, warehouse B has 7 pieces, the shopper has added to cart 15 pieces.
	 *	As long as the shopper is logged in, other shoppers should be able to see only 2 pieces available.
     *
	 *	At this point, we have two cases:
	 *	1) Shopper hits checkout. The system should do the following:
	 *	-reduce the inventory (permanently) to 2 pieces.
	 *	-compute the nearest warehouse to the shopper's city. Let this warehouse be the warehouse in city C.
	 *	-the shipping has to be computed from C.
	 *	-So here what should happen is: the company has to move the merchandise from warehouse A (10 units) and from warehouse B 
	 *   (5 units) [or, if you desire so, 7 units from warehouse B and 8 units from warehouse A] to the warehouse C. 
	 *   This operation is "invisible" for us (that is you can assume the merchandise moves using procedures to be programmed in the future)
     *
	 *	2) Shopper hits logout WITHOUT doing a checkout. Please store the shopping cart and recover the inventory. 
	 *     So to make it clear, the shopping cart contains 15 units from product p, the shopper is logged out, other shoppers 
	 *     see 17 available (in total).
     *
	 *	3) The shopper logs in again. If in the mean time the merchandise sold out (in full or in part) the product in question 
	 *     must be removed from shopping cart (if you are in GUI, the user should get a notification message). If the merchandise is 
	 *     still there go to point 1 above.
	 */
	
	
	
	
	
	public ProjectV1() throws IOException {
		super();
	}

	/**
	 * This method attempts to add quantity units from the product identified by prodID to the cart.
	 * Returns true if the operation succeeds, false, if then operation fails.
	 * 
	 * @param productID
	 * @param quantity
	 * @param sessionID
	 * @param custID
	 * @return
	 */
	public boolean addToShoppingCart(int productID, int quantity, int sessionID, int custID) {
		// your code goes here
		if (User.allUser.containsKey(sessionID) && customer.containsKey(custID) &&
				User.allUser.get(sessionID).type.equals("Shopper")){
			Shopper s = (Shopper)User.allUser.get(sessionID);
			Product prod = null;
			for (Product p: inv.allProduct())
				if (p.ID == productID){
					prod = p;
				}
			if (prod != null){
				return	s.addtoCart(prod, quantity);
			}
		}
		return false;
	}
	
	/** 
	 * Returns the shopping cart content of the shopper userName.
	 * 
	 * An item in the shopping cart must look like this sample:
	 * prodID: 111
	 * center: (An arraylist with components) "Toronto","Barrie"
	 * quantity: (An arraylist with components) 2,3
	 * 
	 * The sample means this particular item on the shopping cart is the product with product ID=111,
	 * the total quantity in the cart is 5, this quantity is taken 2 pieces from the warehouse located in Toronto
	 * and 3 pieces from the warehouse located in Barrie.
	 *  
	 * @param userName
	 * @return
	 */
	public List<ShoppingCartItem> getShoppingCart(String userName) {
		// your code goes here
		User u = null;
		List<ShoppingCartItem> shoppingCart = new ArrayList<ShoppingCartItem>();
		for (int ID : User.allUser.keySet()){
			User u1 = User.allUser.get(ID);
			if (u1.userID.equals(userName))
				u = u1;
		}
		if (u != null && u.type.equals("Shopper")){
			for (Map.Entry<Product, Integer> c : ((Shopper)u).cart.entrySet()){
				Product p = c.getKey();
				int q = c.getValue();
				List<String> distcenters = new ArrayList<String>();
				List<Integer> quantity = new ArrayList<Integer>();
				for (String distcenter: p.distCenters.keySet()){
					if (q != 0){
						distcenters.add(distcenter);
						int q1 = p.distCenters.get(distcenter);
						System.out.println(q1);
						System.out.println(q);
						if (q1 > q){
							q1 -= q;
							quantity.add(q);
							q = 0;
							q1 -= q;
							p.distCenters.put(distcenter, q1);
							System.out.println(p.distCenters.get(distcenter));
						}
						else{
							q -= p.distCenters.get(distcenter);
							quantity.add(p.distCenters.get(distcenter));
							p.distCenters.put(distcenter, 0);
						}
					}
				}
				shoppingCart.add(new ShoppingCartItem(Integer.toString(p.ID), distcenters, quantity));
			}
		}
		return shoppingCart;
	}
	

	/**
	 * This method checks out the shopping cart, generates an order and returns order ID.
	 * It is the equivalent of the checkout button in your GUI screen.
	 * @param sessionID
	 * @param custID
	 * @return
	 * @throws IOException 
	 */
	public int checkout(int custID, int sessionID) throws IOException {
		if (User.allUser.containsKey(sessionID) && customer.containsKey(custID) &&
				User.allUser.get(sessionID).type.equals("Shopper")){
			Shopper s = (Shopper)User.allUser.get(sessionID);
			ArrayList<Product> p = new ArrayList<Product>();
			ArrayList<Integer> q = new ArrayList<Integer>();
			for (Map.Entry<Product, Integer> c : s.cart.entrySet()){
				p.add(c.getKey());
				q.add(c.getValue());
			}
			Order o = new Order(s.address.city, inv.distCenter_list, p, q);
			for (int i = 0; i < o.product.size();i++){
				o.product.get(i).quantity -= o.quantity.get(i);
				o.product.get(i).temp -= o.quantity.get(i);
				o.product.get(i).sold += o.quantity.get(i);
			}
			getShoppingCart(s.userID);
			getDeliveryRoute(o.ID,sessionID);
			double totalPrice = invoiceAmount(o.ID, sessionID);
			System.out.println(totalPrice);
			s.addInvoice(totalPrice);
			s.cart.clear();
			inventoryManager.writeFile(inv);
			return o.ID;
		}
		return 0;
		// your code goes here
		
	}
	
	public class ShoppingCartItem{
		private String prodID;
		private List<String> center;
		private List<Integer> quantity;
			
		public ShoppingCartItem(String prodID, List<String> center,
				List<Integer> quantity) {
			this.prodID = prodID;
			this.center = center;
			this.quantity = quantity;
		}

		public String getProdID() {
			return prodID;
		}

		public void setProdID(String prodID) {
			this.prodID = prodID;
		}
		
		public List<String> getCenter() {
			return center;
		}
		
		public void setCenter(List<String> center) {
			this.center = center;
		}
		
		public List<Integer> getQuantity() {
			return quantity;
		}

		public void setQuantity(List<Integer> quantity) {
			this.quantity = quantity;
		}
	}
}