%define last_element 0

%macro colon 2
    %if last_element != 0
        dq last_element
    %else
        dq 0
    %endif
    db %1, 0
    %define last_element %2
%endmacro