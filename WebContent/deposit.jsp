<!DOCTYPE html>
<html>
	<body>
		<head>
			<title>Deposit</title>
			<link type = "text/css" rel = "stylesheet" href = "css/main.css">
			<link type = "text/css" rel = "stylesheet" href = "css/deposit.css">
		</head>
		
		<div id = "wrapper">
			<div id = "header">
				<h2>Capital 11 Bank</h2>
			</div>
		</div>
		
		<div id = "container">
			<h3>Deposit from your Bank Account</h3>
			
			<form action = "deposit.do" method = "post">
			
				<table>
					<tbody>
						<tr>
							<td><label>Amount: </label>
							<td><input type = "text" name = "depAmt" /></td>
						</tr>
						<tr>
							<td><label></label></td>
							<td><input type = "submit" value = "Deposit" class = "deposit" /></td>
					</tbody>
				</table>
			</form>
			
			<p>
				<a href = "default.do">Back to the main page</a>
			</p>
		</div>
	</body>
</html>