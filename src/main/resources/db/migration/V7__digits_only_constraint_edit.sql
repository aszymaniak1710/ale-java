ALTER TABLE user_info
    DROP CONSTRAINT family_nr_digits_only;

ALTER TABLE user_info
    ADD CONSTRAINT family_nr_digits_only CHECK (
        family_nr IS NULL
            OR array_to_string(family_nr, ',') = ''
            OR array_to_string(family_nr, ',') ~ '^([0-9]{9})(,[0-9]{9})*$'
        );