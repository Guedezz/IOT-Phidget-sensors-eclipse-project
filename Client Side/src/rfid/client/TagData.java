package rfid.client;

public class TagData {
	int tagid;
	String tagvalue;
	int doorid;
	String doorname;

	public TagData(int tagid) {
		super();
		this.tagid = tagid;
		this.tagvalue = null;
		this.doorid = 0;
		this.doorname = "unknown";
	}

	public TagData(String tagvalue) {
		super();
		this.tagvalue = tagvalue;
		this.tagid = 0;
	}
	
	public TagData(int tagid, String tagvalue, int doorid) {
		super();
		this.tagvalue = tagvalue;
		this.tagid = tagid;
		this.doorid = doorid;
	}

	public int getTagid() {
		return tagid;
	}

	public void setTagid(int tagid) {
		this.tagid = tagid;
	}

	public String getTagvalue() {
		return tagvalue;
	}

	public void setTagvalue(String tagvalue) {
		this.tagvalue= tagvalue;
	}
	
	public int getDoorid() {
		return doorid;
	}

	public void setDoorid(int doorid) {
		this.doorid = doorid;
	}
	
	public String getDoorname() {
		return doorname;
	}
	
	public void setDoorname(String doorname) {
		this.doorname = doorname;
	}

	@Override
	public String toString() {
		return "TagData [tagid=" + String.valueOf(tagid) +  ", tagvalue=" + tagvalue + ", doorid=" + String.valueOf(doorid) + ", doorname=" + doorname + "]";
	}
}
