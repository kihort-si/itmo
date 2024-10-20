%define last_element 0

%macro colon 2
    %ifidn %2, %2
        %2:
            dq 0
            db %1, 0
        %define last_element %2
    %else
        %error "Invalid id"
    %endif
%endmacro
