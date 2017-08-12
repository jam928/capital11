<html><head><style>
body {
	background-color: #f1f1f1;
}
</style>
<link rel="stylesheet" type="text/css" href="css/style.css">
  <title>Capital 11 Bank</title>
  <h1 align = "center">User Account Creation</h1>
  
<script type="text/javascript">
	function loadXMLDoc()
	{
	var xmlhttp;
	var k=document.getElementById("username").value;
	var urls="exist.do?username="+k;
	
	if (window.XMLHttpRequest)
	  {
	  xmlhttp=new XMLHttpRequest();
	  }
	else
	  {
	  xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	  }
	xmlhttp.onreadystatechange=function()
	  {
	  if (xmlhttp.readyState==4)
	    {
	        document.getElementById("err").innerHTML=xmlhttp.responseText;
	     }
	  }
	xmlhttp.open("GET",urls,true);
	xmlhttp.send();
	}
</script>
 
</head>
<div class="form">
	<form id="contactform" action = "register.do" method = "post" name = "regF">
	
		<p class="contact"><label for="name">Name</label></p>
		<input id="name" name="name" placeholder="First and last name" required="" tabindex="1" type="text">
		
		<p class="contact"><label for="email">Email</label></p>
		<input id="email" name="email" placeholder="example@domain.com" required="" type="email">
        
 	    <p class="contact"><label for="username">Create a username</label></p>
        <input id="username" name="username" placeholder="username" required="" tabindex="2" type="text" onkeyup="loadXMLDoc()">
        <span id="err"> </span>
        <br>
        
                <p class="contact"><label for="password">Create a password</label></p>
                <input type="password" id="password" name="password" required="" type="text">
               <p class="contact"><label for="repassword">Confirm your password</label></p>
          <input type="password" id="password" name="password" required="" type="text">

               <fieldset>

                 <label>Birthday</label>

                  <label class="month">

                  <select class="select-style" name="BirthMonth">

                  <option value="">Month</option>

                  <option  value="1">January</option>

                  <option value="2">February</option>

                  <option value="3" >March</option>

                  <option value="4">April</option>

                  <option value="5">May</option>

                  <option value="6">June</option>

                  <option value="7">July</option>

                  <option value="8">August</option>

                  <option value="9">September</option>

                  <option value="10">October</option>

                  <option value="11">November</option>

                  <option value="12" >December</option>

                  </label>

                 </select>   

                <label>Day<input class="birthday" maxlength="2" name="BirthDay"  placeholder="Day" required=""></label>

                <label>Year <input class="birthyear" maxlength="4" name="BirthYear" placeholder="Year" required=""></label>

              </fieldset>

 
			Select Gender:
            <select class="select-style gender" name="gender">

            <option value="select">i am..</option>

            <option value="m">Male</option>

            <option value="f">Female</option>

            <option value="others">Other</option>

            </select><br><br>
            
			Select Account Type:
            <select class="select-style acct" name="acct">

            <option value="checking">Checking</option>

            <option value="savings">Savings</option>

            <option value="college">College</option>

            </select><br><br>

            <input class="buttom" name="submit" id="submit" tabindex="5" value="Sign me up!" type="submit">   

   </form>

</div>

</body></html>