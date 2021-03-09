package finalproject.entities;

public class Person implements java.io.Serializable {

	private static final long serialVersionUID = 4190276780070819093L;

	private String first;
	private String last;
    private int age;
    private String city;
    private int ID;
    
    public Person(String first, String last, int age, String city, int ID) {
    	this.first = first;
	    this.last = last;
	    this.city = city;
	    this.age = age;
	    this.ID = ID;
	    }
    
    public String getLastName() {
        return last;
      }

      public String getFirstName() {
        return first;
      }

      public String getCity() {
        return city;
      }

      public int getID() {
        return ID;
      }

      public int getAge() {
        return age;
      }
      
      public String toString() {
    	  return "Person: last name: " + this.last + ", first name: " + this.first + ", city: " + 
    			  	this.city + ", age: " + this.age + ", ID = " + this.ID;
      }
}
