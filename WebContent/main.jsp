<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>

<!DOCTYPE html>
<html>

<head>
	<title>Main</title>
	<link type = "text/css" rel = "stylesheet" href = "css/main.css">
</head>

<body>

	<div id = "wrapper">
		<div id = "header">
			<h2>Capital 11 Bank</h2>
		</div>
	</div>

	<div id = "container">
	
		<div id = "content">
		
		<!-- Add three buttons: Withdrawl,Deposit, and View/Update Info -->
		<input type = "button" value = "withdrawl"
			onclick = "window.location.href = 'withdrawl.jsp'; return false;"
			class = "withdrawl-button"
		/>
		<input type = "button" value = "deposit"
			onclick = "window.location.href = 'deposit.jsp'; return false;"
			class = "deposit-button"
		/>
		<input type = "button" value = "View/Update Info"
			onclick = "window.location.href = 'viewinfo.jsp'; return false;"
			class = "viewinfo-button"
		/>
		
		<h3>Current Balance: $${balance}</h3>
		<h3>Welcome: ${name}</h3>
		
		<table>
			<!-- Set up column names -->
				<tr>
					<th>Date</th>
					<th>Transaction Type</th>
					<th>Amount</th>
				</tr>
			<!-- Print out transactions on the current user account -->
			<c:forEach var = "tempTransaction" items = "${listOfTransactions}">
				<tr>
					<td>${tempTransaction.date}</td>
					<td>${tempTransaction.tr_type}</td>
					<td>$${tempTransaction.amount}</td>
				</tr>
			</c:forEach>
		</table>
		</div>
	</div>
	<p>
		<a href = "logout.do">Logout</a>
	</p>
</body>
</html>