$(document).ready(function() {
  // show error message, then fade out
  $(".error").fadeOut(6000);

  // auto complete
  $( "#query" ).autocomplete({
    source: "/suggestions",
    minLength: 1
  });
});

// validate query
function pass(field, valid_type)
{
  with (field)
  {
    if (valid_type == "empty") {
      if (value == null || value == "" || value.replace(/(^\s*)|(\s*$)/g, "") === "") {
        alert("Query is required.");

        return false;
      } else {
        return true;
      }
    } else {  // malicious
      if (value == "*") {
        alert("Query is invalid.");
        return false;
      } else {
        return true
      }
    }
  }
}

// validate form
function validate_form(form)
{
  with (form)
  {
    if (pass(query, "empty") == false || pass(query, "malicious") == false) {
      query.focus();
      return false;
    }
  }
}