package kalang.compiler

class CompileError  extends RuntimeException{
	
    int start = 0;
	
    int stop = 0;
    
    String className
    
    String source
	
    public CompileError(String msg,String className,String src,int start = 0,int stop = 0){
        super(msg)
        this.start = start
        this.stop = stop
        this.className = className
        this.source = src
    }
	
	public String getErrorSource(){
		if(stop<0) return null;
		if(start<0) return null;
		if(stop<start) return null;
		return source.substring(start,stop)
	}

	@Override
	public String toString() {
		String text = getErrorSource();
		if(text==null || text.length()==0){
			text = "unknown position"
		}
		this.getMessage() + " on ${className}:" + text
	}
	
	
	
}